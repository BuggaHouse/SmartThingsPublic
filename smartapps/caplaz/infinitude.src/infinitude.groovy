/**
 *  Infinitude
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
    name: "Infinitude",
    namespace: "Caplaz",
    author: "Stefano Acerbetti",
    description: "Infinitude is an alternative web service for Carrier Infinity Touch and compatible thermostats.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Infinitude Proxy") {
        paragraph "Infinitude internal host information"
    	input "host", "text", defaultValue: "192.168.7.100", required: true, title: "Host"
    	input "port", "number", defaultValue: "3000", required: true, title: "Port"
	}
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
	// TODO: subscribe to attributes, devices, locations, etc.
	log.debug "Connecting to Infinitude"
    
    def infinitudeIp = settings.host
    def infinitudePort = settings.port
    
     try {
        def hubAction = new physicalgraph.device.HubAction(
        	[
                method: "GET",
                path: "/api/config/zones",
                headers: [ HOST: "$infinitudeIp:$infinitudePort" ] 
            ],
            null,
            [ callback: handlerStatusResponse ]
        )
        sendHubCommand(hubAction)
        
        unschedule(refresh)
        runEvery5Minutes(refresh)
        
    } catch (Exception e) {
		log.error "postToInfluxDB(): Exception ${e} on ${hubAction}"
    }
}

def handlerStatusResponse(physicalgraph.device.HubResponse hubResponse) {

    if (hubResponse.status == 200) {
    	def body = hubResponse.json
      
        body.data.zone.each { zone ->
            if (zone.enabled.getAt(0) == "on") {
            
    			def name = zone.name.getAt(0)
                def dni = app.id + "|zone|" + zone.id
                
                log.info "Found zone: ${name}, DNI: ${dni}"
                
                addThermostat(dni, name)
            }
        }
    } else {
    	log.error "NETWORK ERROR"
    }
}

private addThermostat(dni, name) {
    log.debug "Processing DNI: " + dni
    
    def d = getChildDevice(dni)
    if(!d) {
        d = addChildDevice("SmartThingsMod", "Carrier Thermostat", dni, null, 
        [
        	"label": name,
        	"componentLabel": "Carrier Thermostat Zone " + dni.split("\\|")[2]
        ])
        log.debug "----->created ${d.displayName} with id ${dni}"
        
    } else {
        log.debug "found ${d.displayName} with id ${dni} already exists"
    }
    
    return d
}

def refresh() {
	log.debug "Executing 'poll'"
    
}