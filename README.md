![banner](./banner.png#gh-light-mode-only "banner")
![banner](./banner_dark.png#gh-dark-mode-only "banner")

# Project TrainControl
To implement an autonomous DCC model railway control system with Arduino DCC++ command station and JMRI API.

## Basic Features

* **Acceleration / Deceleration**: emulates the acceleration and deceleration of the train.
* **Distance Control**: controls the train to move for a certain distance, based on the preset speed mapping.
* **Automated Speed Profiling**: automatically measures the throttle-speed mapping and generates the speed profile.  

## Block Section Operation
Enables computed [block section](https://en.wikipedia.org/wiki/Absolute_block_signalling) operation to prevent train collision.

* **Layout Construction and Path Finding**: decodes a JSON-formatted layout file to construct the layout in code. Integrates with Path-finding algorithm to move the train to a particular location in the layout.
* **Turnout Control**: controls turnouts and allows trains to travel into diverging/merging tracks.
* **Crossover Control & Bidirectional Operation**: Controls crossovers and allows trains to cross into the opposite side of the track.
* **Sensor Calibration**: calibrates train's position by reacting to IR sensor events. Improves distance control accuracy under block section operation.

## WIP Features
* **Automatic Train Operation (ATO)**: to have trains in the system operate autonomously.
