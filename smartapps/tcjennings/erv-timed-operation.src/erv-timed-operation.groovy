/**
 *  ERV Timed Operation
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
    name: "ERV Timed Operation",
    namespace: "tcjennings",
    author: "Toby Jennings",
    description: "Reacts to the ERV Ventilation routine and schedules a task 20/40/60-minutes in future to turn the ERV back off.",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("ERV Switches") {
		input "switches", "capability.switch", 
		title: "Which ERV switches?", multiple: true
	}
	
	section("Timer Duration") {
		input "minutes", "number", required: true, title: "How Long (minutes)?"
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
	subscribe(location, "routineExecuted", routineChanged)
}

def routineChanged(evt) {
	if (evt.displayName == "ERV Ventilation") {
		log.debug "${evt.descriptionText}"
		def operationTime = minutes * 60 //n minutes * 60 seconds
		runIn( operationTime, turnOffERV)
	}
}

def turnOffERV() {
	switches.each {
    	log.debug "Turning off ${it}."
		it.off()
	}
}


