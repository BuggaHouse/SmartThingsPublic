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
    getZonesConfig()

    // refresh the devices every 5 minuts
    unschedule(refresh)
    runEvery5Minutes(refresh)
}

def refresh() {
    log.debug "Executing 'poll'"

    def children = getAllChildDevices()
    children.each { child ->
        def zoneId = dniToZone(child.deviceNetworkId)
        getZoneStatus(zoneId)
    }
}

def refreshChild(dni) {
	log.debug "Refreshing for child " + dni
	//def dni = [ app.id, systemID, zone.'@id'.text().toString() ].join('|')

    def d = getChildDevice(dni)
    if(d) {
    	d.zUpdate(state.data[dni].temperature,
            state.data[dni].thermostatOperatingState,
            state.data[dni].humidity,
            state.data[dni].heatingSetpoint,
            state.data[dni].coolingSetpoint,
            state.data[dni].thermostatFanMode,
            state.data[dni].thermostatActivityState)
		log.debug "Data sent to DH"
        
        
                            //    temperature: zone.rt.toInteger(),
                              //  humidity: zone.rh.toInteger(),
                              //  coolingSetpoint: zone.clsp.toInteger(),
                              //  heatingSetpoint: zone.htsp.toInteger(),
                              //  thermostatFanMode: lookupInfo( "thermostatFanMode", zone.fan.text().toString(), true ),
                              //  thermostatOperatingState: lookupInfo( "thermostatOperatingState", response.data.mode.text().toString(), true ),
                               // thermostatActivityState: zone.currentActivity.text().toString(),
                                
	}
    else {
    	log.debug "Somethig went wrong!"
    }
}



/***********
 * CONFIG *
 ***********/

private getZonesConfig() {
    log.debug "Connecting to Infinitude"
    
    try {
        def hubAction = new physicalgraph.device.HubAction(
            [
                method: "GET",
                path: "/api/config/zones",
                headers: [ HOST: getInfinitudeHost() ] 
            ],
            null,
            [ callback: getZonesConfigParser ]
        )
        sendHubCommand(hubAction)

    } catch (Exception e) {
        log.error "getZonesConfig(): Exception ${e} on ${hubAction}"
    }
}

def getZonesConfigParser(physicalgraph.device.HubResponse hubResponse) {
    if (hubResponse.status == 200) {
        def body = hubResponse.json

        body.data.zone.each { zone ->
            if (zone.enabled.getAt(0) == "on") {

                def name = zone.name.getAt(0)
                def dni  = zoneToDni(zone.id)

                log.info "Found zone: ${name}, DNI: ${dni}"

                addThermostat(dni, name)
            }
        }
    } else {
        log.error "NETWORK ERROR"
    }
}





/***********
 * STATUS  *
 ***********/

private getZoneStatus(zoneId) {
    log.debug "Updating status of zone ${zoneId}"
    
    try {
        def hubAction = new physicalgraph.device.HubAction(
            [
                method: "GET",
                path: "/api/status/${zoneId}",
                headers: [ HOST: getInfinitudeHost() ] 
            ],
            null,
            [ callback: getZoneStatusParser ]
        )
        sendHubCommand(hubAction)

    } catch (Exception e) {
        log.error "getZoneStatus(): Exception ${e} on ${hubAction}"
    }
}

def getZoneStatusParser(physicalgraph.device.HubResponse hubResponse) {
    if (hubResponse.status == 200) {
        def zone = hubResponse.json
        def dni  = zoneToDni(zone.id)

        log.debug "Refreshing for zone " + dni

        def device = getChildDevice(dni)
        if (device) {
            device.updateState(zone.id, zone)
            
            log.debug "Data sent to DH"

        } else {
            log.error "Somethig went wrong!"
        }

    } else {
        log.error "NETWORK ERROR"
    }
}



/***********
 * CHANGE  *
 ***********/

def setHold(zoneId, activity, hold = 'on', until = '23:45') {
    def command = "hold=${hold}&activity=${activity}&until=${until}"
    
    log.debug "Set hold for zone:${zoneId} with ${command}"

    try {
        def hubAction = new physicalgraph.device.HubAction(
            [
                method: "POST",
                path: "/api/${zoneId}/hold",
                body: command,
                headers: [ 
                    HOST: getInfinitudeHost(), 
                    "Content-Type": "application/x-www-form-urlencoded" 
                ]
            ],
            null,
            [ callback: setHoldParser ]
        )

        sendHubCommand(hubAction)

        return true

    } catch (Exception e) {
        log.error "getZonesConfig(): Exception ${e} on ${hubAction}"
        
        return false
    }
}

def setHoldParser(physicalgraph.device.HubResponse hubResponse) {
    if (hubResponse.status != 200) {
        log.error "Error setting the hold: ${hubResponse}"
    }
}




/***********
 * HELPERS *
 ***********/

private getInfinitudeHost() {
    def infinitudeIp = settings.host
    def infinitudePort = settings.port

    return "${infinitudeIp}:${infinitudePort}"
}

private zoneToDni(zoneId) {
    return app.id + "|zone|" + zoneId
}

private dniToZone(dni) {
    return dni.split("\\|")[2]
}

private addThermostat(dni, name) {
    log.debug "Processing DNI: " + dni

    def d = getChildDevice(dni)
    if(!d) {
        d = addChildDevice("SmartThingsMod", "Carrier Thermostat", dni, null, 
                           [
                               "label": name,
                               "componentLabel": "Carrier Thermostat Zone " + dniToZone(dni)
                           ])
        log.debug "----->created ${d.displayName} with id ${dni}"

    } else {
        log.debug "found ${d.displayName} with id ${dni} already exists"
    }

    return d
}