# Math-Aid
The authoring component of a voice-activated Mathematics tutoring aid.

### Requirements
* A linux machine
* Bash shell
* Appropriate libraries for javafx media player (see javafx reqs)

### How to execute
Open a terminal in the directory where the jar is stored. Run the command: <br />
For the javafx gui: java -jar Math-Aid.jar <br />
For the Bash CLI: bash maths_aid

### Functionality
Allows a user to create a video with some customized text and audio.  <br />
The user can play and delete these creations using the gui.

### Invalid Characters
A creation name cannot contain the following characters: $, \\, ", .  <br />
Leading and trailing spaces are ignored.  <br />
Two creations of the same name cannot exist, however, already existing creations can be overwritten.

###### Nathan Cairns
