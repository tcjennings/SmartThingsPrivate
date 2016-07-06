/**
 *  Track Sensor Data
 *
 *  Copyright 2016 Toby Jennings
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
    name: "Track Sensor Data",
    namespace: "tcjennings",
    author: "Toby Jennings",
    description: "This smart app subscribes to sensors and pushes the updates to a local (to the hub) Elasticsearch database.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)


preferences {
    section("Select Temperature Sensors") {
        input "sensors", "capability.temperatureMeasurement", required: true,  multiple: true, title: "Temperature Sensors:"
    }
    section("Select Humidity Sensors") {
        input "humsensors", "capability.relativeHumidityMeasurement", required: true,  multiple: true, title: "Humidity Sensors:"
    }

    section("Select Switches") {
        input "switches", "capability.switch", required: true,  multiple: true, title: "Switches:"
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
	subscribe(sensors, "temperature", sensorHandler)
    subscribe(humsensors, "humidity", humidityHandler)
    subscribe(switches, "switch", switchHandler)
}

def sendCommand(json) {
  def headers = [:] //an empty map
  headers.put("HOST", "192.168.1.179:9200")
  headers.put("Content-Type", "application/x-www-form-urlencoded")

	//Send this json to Elasticsearch
  def result = new physicalgraph.device.HubAction(
    method: "POST",
    path: "/smartthings/event",
    headers: headers,
    body: json.toString()
	)
    //log.debug(result)
    
    sendHubCommand(result)
    return result
}

def sensorHandler(evt) {
	def evtValue
	try{
		evtValue = evt.doubleValue
	} catch (e) {
		evtValue =  evt.stringValue
	}

    def json
	try {
		def data = [
        	date: evt.isoDate,
            name: evt.displayName,
            event: [
            	sensor: "temperature",
                value: evtValue
            ]
        ]
       json = new groovy.json.JsonBuilder(data)
	   //log.debug "${json.toPrettyString()}"
       sendCommand(json)
    } catch (e) {
       log.debug "Trying to build Json for ${evt.name} threw an exception: $e"
    }
}

def switchHandler(evt) {
	def evtValue
    def binValue
   	evtValue = evt.stringValue
    binValue = evt.value == "on" ? 1 : 0
    log.debug "${evt.displayName} is status ${binValue}"
    
    def json
    try {
    	def data = [
        	date: evt.isoDate,
            name: evt.displayName,
            event: [
            	sensor: "switch",
                value: binValue
              ]
             ]
        json = new groovy.json.JsonBuilder(data)
        sendCommand(json)
       } catch (e) {
       log.debug "Trying to build Json for ${evt.name} threw an exception: $e"
    }       
}

def humidityHandler(evt) {
	def evtValue
	try{
		evtValue = evt.doubleValue
	} catch (e) {
		evtValue =  evt.stringValue
	}

    def json
	try {
		def data = [
        	date: evt.isoDate,
            name: evt.displayName,
            event: [
            	sensor: "humidity",
                value: evtValue
            ]
        ]
       json = new groovy.json.JsonBuilder(data)
	   //log.debug "${json.toPrettyString()}"
       sendCommand(json)
    } catch (e) {
       log.debug "Trying to build Json for ${evt.name} threw an exception: $e"
    }
}