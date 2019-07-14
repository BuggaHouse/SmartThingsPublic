/**
 *  Orbit B&bull;Hyve
 *
 *  Copyright 2019 Stefano Acerbetti
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
 
definition(
    name: "Orbit B•Hyve",
    namespace: "Caplaz",
    author: "Stefano Acerbetti",
    description: "Sprinkler controller",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)


preferences {
    page(name:"mainMenu")
    page(name:"mainOptions")
}

def installed() {
    log.debug "Installed with settings: ${settings}"

    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"

    unsubscribe()
    initialize()
}

def initialize() {
    updateDevices()
    setScheduler(schedulerFreq)
}

def refresh() {
    log.info "Executing refresh"
    updateDevices()
}

def updateDevices() {
    def deviceData = bHyveDevices()
    if (deviceData != null) {
               log.debug "response data: ${deviceData}"
                
        deviceData.eachWithIndex { it, index ->
            log.info "Device (${index}): ${it.name} is a ${it.type} and last connected at: ${it.last_connected_at}"

        }
    }
}



/*************************************************************
 *				      PREFERENCES							 *
 *************************************************************/

def mainMenu() {
    def orbitBhyveLoginOK = false

    if ( (username) && (password) ) {
        orbitBhyveLoginOK = bHyveLogin()
        log.debug "orbitBhyveLoginOK= ${orbitBhyveLoginOK}"
    }

    dynamicPage(
        name: "mainMenu",
        title: "Orbit B•Hyve™ Timer Account Login Information",
        nextPage: (orbitBhyveLoginOK) ? "mainOptions" : null,
        submitOnChange: true,
        install: false,
        uninstall: true)
    {
        if ( (username) && (password) ) {
            if (state?.orbit_session_token) {
                section("Orbit B•Hyve™ Information") {
                    paragraph "Your Login Information is Valid"
                    paragraph image : getAppImg("icons/success-icon.png"),
                        title: "Account name: ${state.user_name}",
                        required: false,
                        state.devices
                }
                section {
                    href(name: "Orbit B•Hyve™ Timer Options",
                         page: "mainOptions",
                         description: "Complete Orbit B•Hyve™ Options")
                }
            } else {
                section("Orbit B•Hyve™ Status/Information") {
                    paragraph "Your Login Information is INVALID"
                    paragraph image: getAppImg("icons/failure-icon.png"),
                        required: false,
                        title: "$state.statusText",
                        ""
                }
            }
        }

        section () {
            input (
                name     : "username",
                type     : "text",
                title    : "Account userid",
                submitOnChange: true,
                multiple : false,
                required : true
            )
            input ( 
                name     : "password",
                type     : "password",
                title    : "Account password",
                submitOnChange: true,
                multiple : false,
                required : true
            )
        }
    }
}

def mainOptions() {
    dynamicPage(
        name: "mainOptions",
        title: "Bhyve Timer Controller Options",
        install: true,
        uninstall: false)
    {
        section("Spa Refresh Update Interval") {
            input ( 
                name: "schedulerFreq",
                type: "enum",
                title: "Run Bhyve Refresh Every (X mins)?",
                options: [
                    '0':'Off',
                    '1':'1 min',
                    '5':'5 mins',
                    '10':'10 mins',
                    '15':'15 mins',
                    '30':'Every ½ Hour',
                    '60':'Every Hour',
                    '180':'Every 3 Hours'
                ],
                required: true
            )
            mode ( 
                title: "Limit Polling Bhyve to specific ST mode(s)",
                image: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
                required: false
            )
        }
        section("Bhyve Notifications") {
            input ( 
                name	: "phone",
                type	: "phone",
                title	: "Text Messages for Alerts (optional)",
                description: "Mobile Phone Number",
                required: false
            )
        }
        section() {
            label ( 
                name: "name",
                title: "This SmartApp's Name",
                state: (name ? "complete" : null),
                defaultValue: "${app.name}",
                required: false
            )
        }
    }
}



/*************************************************************
 *						ACTIONS								 *
 *************************************************************/

def bHyveLogin() {
    if ((username == null) || (password == null)) {
        return false

    } else {
        def params = [
            'uri': "https://api.orbitbhyve.com/v1/",
            'path': "session",
            'body': [
                'session': [
                    'email'   : username,
                    'password': password
                ]
            ]
        ]

        try {
            httpPostJson(params) { resp ->
                if (resp.status == 200) {
                    log.debug "HttpPost Login Request was OK ${resp.status}"

                    state.orbit_session_token 	= "${resp.data?.orbit_session_token}"
                    state.user_id 				= "${resp.data?.user_id}"
                    state.user_name 			= "${resp.data?.user_name}"
                    state.statusText			= "Success"

                } else {
                    log.error "Fatal Error Status '${resp.status}' from Orbit B•Hyve™ Login.  Data='${resp.data}' at ${timeString}."

                    state.orbit_session_token 	= null
                    state.user_id 				= null
                    state.user_name 			= null
                    state.statusText 			= "Fatal Error Status '${resp.status}' from Orbit B•Hyve™ Login.  Data='${resp.data}' at ${timeString}."

                    return false
                }
            }

        } catch (Exception e) {
            log.debug "Catch HttpPost Login Error: ${e}"

            state.orbit_session_token 	= null
            state.user_id 				= null
            state.user_name 			= null
            state.statusText 			= "Fatal Error for Orbit B•Hyve™ Login '${e}'"

            return false
        }

        return true
    }
}

def bHyveDevices() {

    def devices = null
    
    if (state.orbit_session_token != null) {

        def params = [
            'uri': "https://api.orbitbhyve.com/v1/",
            'headers': ['orbit-session-token': state.orbit_session_token],
            'path': "devices",
            "query": ["user_id": state.user_id]
        ]

        try {
            httpGet(params) { resp ->
                // log.debug "response data: ${resp.data}"

                if (resp.status == 200) {
                    log.debug "HttpGet Devices Request was OK ${resp.status}"
                    devices = resp.data

                } else {
                    log.error "Fatal Error Status '${resp.status}' from Orbit B•Hyve™ ${command}.  Data='${resp?.data}'."
                }
            }

        } catch (e) {
            log.error "bHyveDevices: something went wrong: ${e}"
        }
    }

    return devices
}



/*************************************************************
 *						PRIVATE								 *
 *************************************************************/

private String getAppImg(imgName) {
    return "https://raw.githubusercontent.com/KurtSanders/STOrbitBhyveTimer/master/images/$imgName" 
}

private setScheduler(schedulerFreq) {
    state.schedulerFreq = "${schedulerFreq}"
    
    switch(schedulerFreq) {
        case '0':
            unschedule()
            break
        
        case '1':
            runEvery1Minute(refresh)
            break
        
        case '5':
            runEvery5Minutes(refresh)
            break
        
        case '10':
            runEvery10Minutes(refresh)
            break
        
        case '15':
            runEvery15Minutes(refresh)
            break
        
        case '30':
            runEvery30Minutes(refresh)
            break
        
        case '60':
            runEvery1Hour(refresh)
            break
        
        case '180':
            runEvery3Hours(refresh)
            break
        
        default :
            log.warn "Unknown Schedule Frequency"
            unschedule()
            return
    }
    
    if (schedulerFreq == '0') {
        log.debug "UNScheduled all RunEvery"
        
    } else {
        log.debug "Scheduled RunEvery${schedulerFreq}Minute"
    }
}