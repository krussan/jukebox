# jukebox
Yes. It's another movie jukebox identifier.

Still an early alpha version release.

It identifies your videos, lookup information on them and tries to 
download subtitles (currently just english) for your specific video.

It has it's own android app (not available yet through Play Store but 
can be installed manually) from which you can browse the repository and 
start your videos.
Frontends for the video playback include Google Chromecast, VLC (RC or 
web) and the bundled JukeboxFront (very much wip on this one)

## Installation
- unpack the zip file
- modify the settings.
- start up
- watch the logs

## Android app
### Installation
### Startup
Be sure to click on the three dots on the main screen to set the ip and 
port number of your server. After that you are good to go. Push the 
refresh button and your repository will hopefully appear.

## Jukebox settings
The following defines the nodes and attributes of the JukeboxListenerSettings.xml
An [A] specifies that the term is an attribute.

### catalogs 
defines the catalogs that	are going to be	scanned
- [A] includeSubDirectories - true if server should scan all subdirectories
- [A] path - specifies the path to scan for medi
-- localPaths/path
-- [A] player - specifies the name of the player to appear in app
-- [A] path - the path to this catalog where the player is situated. I.e. a network path if the player is JukeboxFront or VLC. For chromecast leave blank
-- extensions/extension - lists a number of filename extensions to look for
-- [A] value - an extension to look for

### subFinders
this section/node defines different subtitle finders. For advanced users only.
-- subFinder
-- [A] class - The full qualified path to the java class extending the SubFinderBase class
-- [A] enabled - specifies true/false if this subfinder is enabled or not
-- subFinderSettings/setting - defines zero or more settings passed to the SubFinder. Each setting node contains one key and one value node.

### builders
Defines the media identifiers. Advanced use only. Leave as if to use the default identifiers.
-- builder
-- [A] class - The fully qualified class name of the java class extending the MovieBuilder class
-- [A] enabled - specifies true/false if this builder is enabled
.. [A] weight - Specifies a weight to apply to the result. This could be used to favorize a certain builder in front of others.

### tcpListener
Specifies the port that the server listens for incoming requests
-- port
-- [A] value - Specifies the port

## Work to do
Ther is still a lot of work to do to get this up and running.
Browse the github repository for all issues currently under consideration or review.

