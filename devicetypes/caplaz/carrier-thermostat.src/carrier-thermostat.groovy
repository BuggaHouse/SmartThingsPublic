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

metadata {
    definition (name: "Carrier Thermostat", namespace: "Caplaz", author: "Stefano Acerbetti") {
        capability "Health Check"
        capability "Relative Humidity Measurement"
        capability "Refresh"
        capability "Thermostat"

        command "setActivityPerSchedule"
        command "setActivityHome"
        command "setActivityAway"
        command "setActivityWake"
        command "setActivitySleep"
        
		command "tempUp"
		command "tempDown"
		command "heatUp"
		command "heatDown"
		command "coolUp"
		command "coolDown"
		command "setTemperature", ["number"]

        attribute "zoneId", "number"
        attribute "currentActivity", "string"
        attribute "hold", "string"
        attribute "holdUntil", "string"
    }

    tiles(scale: 2) {
        multiAttributeTile(name:"thermostat", type:"thermostat", width:6, height:4, canChangeIcon: true) {
            tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
                attributeState("temperature", label:'${currentValue}°', unit:"dF", defaultState: true, 
                               backgroundColors:[
                                   [value: 31, color: "#153591"],
                                   [value: 44, color: "#1e9cbb"],
                                   [value: 59, color: "#90d2a7"],
                                   [value: 74, color: "#44b621"],
                                   [value: 84, color: "#f1d801"],
                                   [value: 95, color: "#d04e00"],
                                   [value: 96, color: "#bc2323"]
                               ])
            }
            tileAttribute("device.temperature", key: "VALUE_CONTROL") {
                attributeState("VALUE_UP", action: "tempUp")
                attributeState("VALUE_DOWN", action: "tempDown")
            }
            tileAttribute("device.humidity", key: "SECONDARY_CONTROL") {
                attributeState("humidity", label:'${currentValue}%', unit:"%", defaultState: true)
            }
            tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
                attributeState("idle", backgroundColor:"#00A0DC")
                attributeState("heating", backgroundColor:"#e86d13")
                attributeState("cooling", backgroundColor:"#00A0DC")
            }
            tileAttribute("device.thermostatMode", key: "THERMOSTAT_MODE") {
                attributeState("off", label:'${name}')
                attributeState("heat", label:'${name}')
                attributeState("cool", label:'${name}')
                attributeState("auto", label:'${name}')
            }
            tileAttribute("device.heatingSetpoint", key: "HEATING_SETPOINT") {
                attributeState("heatingSetpoint", label:'${currentValue}', unit:"dF", defaultState: true)
            }
            tileAttribute("device.coolingSetpoint", key: "COOLING_SETPOINT") {
                attributeState("coolingSetpoint", label:'${currentValue}', unit:"dF", defaultState: true)
            }
        }

        standardTile("activity", "device.currentActivity", width: 2, height: 2) {
            state "away",   label:'${currentValue}', icon: "st.Health & Wellness.health12", backgroundColor:"#7eb26d"
            state "wake",   label:'${currentValue}', icon: "st.Lighting.light1", backgroundColor:"#f9934e"
            state "sleep",  label:'${currentValue}', icon: "st.Bedroom.bedroom2", backgroundColor:"#614d93"
            state "home",   label:'${currentValue}', icon: "st.Home.home4", backgroundColor:"#1f78c1"
            state "manual", label:'${currentValue}', icon: "st.Home.home1", backgroundColor:"#d683ce"
        }
        standardTile("setHome", "device.currentActivity", decoration: "flat", width: 2, height: 1)	{
            state "val", label:"Set Home", icon: "st.Home.home4", action: "setActivityHome"
        }
        standardTile("setAway", "device.currentActivity", decoration: "flat", width: 2, height: 1)	{
            state "val", label:"Set Away", icon: "st.Health & Wellness.health12", action: "setActivityAway"
        }
        standardTile("setWake", "device.currentActivity", decoration: "flat", width: 2, height: 1)	{
            state "val", label:"Set Wake", icon: "st.Lighting.light1", action: "setActivityWake"
        }
        standardTile("setSleep", "device.currentActivity", decoration: "flat", width: 2, height: 1)	{
            state "val", label:"Set Sleep", icon: "st.Bedroom.bedroom2", action: "setActivitySleep"
        }

/*
		valueTile("heatingSetpoint", "device.heatingSetpoint", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "heat", label:'${currentValue} heat', unit: "F", backgroundColor:"#ffffff"
		}
		standardTile("heatDown", "device.temperature", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "heatDown", label:'down', action:"heatDown", defaultState: true
		}
		standardTile("heatUp", "device.temperature", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "heatUp", label:'up', action:"heatUp", defaultState: true
		}

		valueTile("coolingSetpoint", "device.coolingSetpoint", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "cool", label:'${currentValue} cool', unit:"F", backgroundColor:"#ffffff"
		}
		standardTile("coolDown", "device.temperature", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "coolDown", label:'down', action:"coolDown", defaultState: true
		}
		standardTile("coolUp", "device.temperature", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "coolUp", label:'up', action:"coolUp", defaultState: true
		}
*/

        valueTile("operatingState", "device.thermostatOperatingState", decoration: "flat", width: 4, height: 1) {
            state "val", label:'State: ${currentValue}'
        }
        standardTile("refresh", "device.thermostatMode", width:2, height:1, inactiveLabel: false, decoration: "flat") {
            state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
        }

        // the tile will appear in the Things view
        main "thermostat"

        // the tiles will appear in the Device Details
        // view (order is left-to-right, top-to-bottom)
        details([
            "thermostat",  
            "activity", "setHome", "setAway", "setWake", "setSleep",
			"temperature", "tempDown", "tempUp",
			"heatingSetpoint", "heatDown", "heatUp",
			"coolingSetpoint", "coolDown", "coolUp",
            "operatingState", "refresh"
        ])
    }

    preferences {
        input "holdType", "enum", title: "Hold Type",
            description: "When changing temperature, use Temporary (Until next transition) or Permanent hold (default)",
            required: false, options:["Temporary", "Permanent"]

        input "deadbandSetting", "number", title: "Minimum temperature difference between the desired Heat and Cool " +
            "temperatures in Auto mode:\nNote! This must be the same as configured on the thermostat",
            description: "temperature difference °F", defaultValue: 5,
            required: false
    }

}

def installed() {
    initialize()
}

def updated() {
    initialize()
}

def initialize() {
    parent.refresh()
}

def updateState(zoneId, zone) {

    // extract the attributes
    def temperature     = zone.rt.get(0)
    def humidity        = zone.rh.get(0)
    def coolingSetpoint = zone.clsp.get(0)
    def heatingSetpoint = zone.htsp.get(0)
    
    def hold = zone.hold.get(0)
    def holdUntil = zone.otmr.get(0)

    def currentActivity  		 = zone.currentActivity.get(0)
    def thermostatMode  		 = "heat"//getThermostatMode(zone.htsp.get(0))
    def thermostatFanMode  		 = getThermostatFanMode(zone.fan.get(0))
    def thermostatOperatingState = getThermostatOperatingState(zone.zoneconditioning.get(0))

    log.debug "UpdateState for zone ${zoneId}: temp=${temperature}, humidity=${humidity}"

    sendEvent([name: "zoneId", value: zoneId])

    sendEvent([name: "temperature", value: temperature, unit: "F"])
    sendEvent([name: "humidity", value: humidity])
    sendEvent([name: "heatingSetpoint", value: heatingSetpoint])
    sendEvent([name: "coolingSetpoint", value: coolingSetpoint])

    sendEvent([name: "currentActivity", value: currentActivity])
    sendEvent([name: "thermostatMode", value: thermostatMode])
    sendEvent([name: "thermostatFanMode", value: thermostatFanMode])
    sendEvent([name: "thermostatOperatingState", value: thermostatOperatingState])
    
    sendEvent([name: "hold", value: hold])
    sendEvent([name: "holdUntil", value: holdUntil])
}

def evaluate(temp, heatingSetpoint, coolingSetpoint) {
	log.debug "evaluate($temp, $heatingSetpoint, $coolingSetpoint"
	def threshold = 1.0
	def current = device.currentValue("thermostatOperatingState")
	def mode = device.currentValue("thermostatMode")

	def heating = false
	def cooling = false
	def idle = false
	if (mode in ["heat","emergency heat","auto"]) {
		if (heatingSetpoint - temp >= threshold) {
			heating = true
			sendEvent(name: "thermostatOperatingState", value: "heating")
		}
		else if (temp - heatingSetpoint >= threshold) {
			idle = true
		}
		sendEvent(name: "thermostatSetpoint", value: heatingSetpoint)
	}
	if (mode in ["cool","auto"]) {
		if (temp - coolingSetpoint >= threshold) {
			cooling = true
			sendEvent(name: "thermostatOperatingState", value: "cooling")
		}
		else if (coolingSetpoint - temp >= threshold && !heating) {
			idle = true
		}
		sendEvent(name: "thermostatSetpoint", value: coolingSetpoint)
	}
	else {
		sendEvent(name: "thermostatSetpoint", value: heatingSetpoint)
	}

	if (mode == "off") {
		idle = true
	}

	if (idle && !heating && !cooling) {
		sendEvent(name: "thermostatOperatingState", value: "idle")
	}
}



/***********************
 *       REFRESH       *
 ***********************/
 
def refresh() {
    log.debug "refresh, calling parent ..."
    parent.refresh()
}

def ping() {
    //def isAlive = device.currentValue("deviceAlive") == "true" ? true : false
    return true
}



/***********************
 *     TEMPERATURE     *
 ***********************/

def setTemperature(value) {
	def ts = device.currentState("temperature")
	sendEvent(name:"temperature", value: value)
	evaluate(value, device.currentValue("heatingSetpoint"), device.currentValue("coolingSetpoint"))
}

def tempUp() {
	def ts = device.currentState("temperature")
	def value = ts ? ts.integerValue + 1 : 72
	sendEvent(name:"temperature", value: value)
	evaluate(value, device.currentValue("heatingSetpoint"), device.currentValue("coolingSetpoint"))
}

def tempDown() {
	def ts = device.currentState("temperature")
	def value = ts ? ts.integerValue - 1 : 72
	sendEvent(name:"temperature", value: value)
	evaluate(value, device.currentValue("heatingSetpoint"), device.currentValue("coolingSetpoint"))
}

def heatUp() {
	def ts = device.currentState("heatingSetpoint")
	def value = ts ? ts.integerValue + 1 : 68
	sendEvent(name:"heatingSetpoint", value: value)
	evaluate(device.currentValue("temperature"), value, device.currentValue("coolingSetpoint"))
}

def heatDown() {
	def ts = device.currentState("heatingSetpoint")
	def value = ts ? ts.integerValue - 1 : 68
	sendEvent(name:"heatingSetpoint", value: value)
	evaluate(device.currentValue("temperature"), value, device.currentValue("coolingSetpoint"))
}

def coolUp() {
	def ts = device.currentState("coolingSetpoint")
	def value = ts ? ts.integerValue + 1 : 76
	sendEvent(name:"coolingSetpoint", value: value)
	evaluate(device.currentValue("temperature"), device.currentValue("heatingSetpoint"), value)
}

def coolDown() {
	def ts = device.currentState("coolingSetpoint")
	def value = ts ? ts.integerValue - 1 : 76
	sendEvent(name:"coolingSetpoint", value: value)
	evaluate(device.currentValue("temperature"), device.currentValue("heatingSetpoint"), value)
}



/***********************
 *      THERMOSTAT     *
 ***********************/
 
def setCoolingSetpoint(setpoint) {
    log.debug "Set cooling point to ${setpoint} for activity ${device.currentValue("currentActivity")}"
    
	sendEvent(name: "coolingSetpoint", value: setpoint)
	evaluate(device.currentValue("temperature"), device.currentValue("heatingSetpoint"), setpoint)
}

def setHeatingSetpoint(setpoint) {
    log.debug "Set heating point to ${setpoint} for activity ${device.currentValue("currentActivity")}"
    
	sendEvent(name: "heatingSetpoint", value: setpoint)
	evaluate(device.currentValue("temperature"), setpoint, device.currentValue("coolingSetpoint"))
}


def setThermostatMode(String value) {
	sendEvent(name: "thermostatMode", value: value)
	evaluate(device.currentValue("temperature"), device.currentValue("heatingSetpoint"), device.currentValue("coolingSetpoint"))
}

def off() {
	sendEvent(name: "thermostatMode", value: "off")
	evaluate(device.currentValue("temperature"), device.currentValue("heatingSetpoint"), device.currentValue("coolingSetpoint"))
}

def heat() {
	sendEvent(name: "thermostatMode", value: "heat")
	evaluate(device.currentValue("temperature"), device.currentValue("heatingSetpoint"), device.currentValue("coolingSetpoint"))
}

def auto() {
	sendEvent(name: "thermostatMode", value: "auto")
	evaluate(device.currentValue("temperature"), device.currentValue("heatingSetpoint"), device.currentValue("coolingSetpoint"))
}

def emergencyHeat() {
	sendEvent(name: "thermostatMode", value: "emergency heat")
	evaluate(device.currentValue("temperature"), device.currentValue("heatingSetpoint"), device.currentValue("coolingSetpoint"))
}

def cool() {
	sendEvent(name: "thermostatMode", value: "cool")
	evaluate(device.currentValue("temperature"), device.currentValue("heatingSetpoint"), device.currentValue("coolingSetpoint"))
}


def setThermostatFanMode(String value) {
	sendEvent(name: "thermostatFanMode", value: value)
}

def fanOn() {
	sendEvent(name: "thermostatFanMode", value: "fanOn")
}

def fanAuto() {
	sendEvent(name: "thermostatFanMode", value: "fanAuto")
}

def fanCirculate() {
	sendEvent(name: "thermostatFanMode", value: "fanCirculate")
}



/***********************
 *       CUSTOM        *
 ***********************/

def setActivityPerSchedule() {
    log.debug "Switch to schedule"

    if (!(parent.setHold(device.currentValue("zoneId"), "", "off", ""))) {
        log.warn "Error removing the hold"
    }
}

def setActivityHome() {
    switchActivityTo("home");
}

def setActivityAway() {
    switchActivityTo("away");
}

def setActivityWake() {
    switchActivityTo("wake");
}

def setActivitySleep() {
    switchActivityTo("sleep");
} 

private switchActivityTo(activity) {
    log.debug "Switch to mode: ${activity}"

    if (!(parent.setHold(device.currentValue("zoneId"), activity))) {
        log.warn "Error setting mode: ${activity}"

    } else {
        sendEvent([name: "currentActivity", value: activity])
    }
}




/***********************
 *       HELPERS       *
 ***********************/

private getThermostatMode(mode) {
    log.debug "Thermostat mode: ${mode}"

    switch (mode) {
        case "cool":
        	return "cool"

        case "heat":
        	return "heat"

        case "auto":
        	return "auto"

        default:
            return "off"
    }
}

private getThermostatFanMode(mode) {
    log.debug "Fan mode: ${mode}"

    switch (mode) {
        case "off":
        	return "auto"

        case "low":
        	return "circulate"

        default:
            return "on"
    }
}

private getThermostatOperatingState(state) {
    log.debug "Operating state: ${state}"

    switch (state) {
    	case "active_heat":
        case "heat":
        case "hpheat":
        	return "heating"

        case "cool":
        	return "cooling"

        case "fanonly":
        	return "fan only"

        default:
            return "idle"
    }
}