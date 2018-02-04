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
defines the catalogs that are going to be scanned

* [A] _includeSubDirectories_ - true if server should scan all subdirectories
* [A] _path_ - specifies the path to scan for media 
  * **localPaths/path**
    * [A] _player_ - specifies the name of the player to appear in app
    * [A] _path_ - the path to this catalog where the player is situated. I.e. a network path if the player is JukeboxFront or VLC. For chromecast leave blank
    
  * **extensions/extension** - lists a number of filename extensions to look for
    * [A] _value_ - an extension to look for

### subFinders
this section/node defines different subtitle finders. For advanced users only.
* subFinder
  * [A] _class_ - The full qualified path to the java class extending the SubFinderBase class
  * [A] _enabled_ - specifies true/false if this subfinder is enabled or not
  * **subFinderSettings/setting** - defines zero or more settings passed to the SubFinder. Each setting node contains one key and one value node.

### builders
Defines the media identifiers. Advanced use only. Leave as if to use the default identifiers.

* builder
  * [A] _class_ - The fully qualified class name of the java class extending the MovieBuilder class
  * [A] _enabled_ - specifies true/false if this builder is enabled
  * [A] _weight_ - Specifies a weight to apply to the result. This could be used to favorize a certain builder in front of others.

### tcpListener
Specifies the port that the server listens for incoming requests

* port
  * [A] _value_ - Specifies the port

### players
Configures the players present in the network

* player
  * [A] _name_ - The name of the player to appear in app
  * [A] _host_ - The ip address of the player if running jukeboxFront with wake-on-lan enabled
  * [A] _broadcastAddress_ - The broadcast address used for wake-on-lan
  * [A] _port_ - TBD
  * [A] _subsPath_ - The remote path from the player to access the sub files
  * [A] _macAddress_ - The mac address of the player used for wake-on-lan
  * [A] _hibernatorPort_ - The port used for the hibernator program
  * [A] _type_ - Specifies the type of the player. Valid values are VLC, JukeboxFront. Chromecast does not need any players defined.
  
### logs
Specifies the log files to log to

* **logs/log**
  * [A] _type_ - Defines the log type. Set to "File"
  * [A] _filename_ - The filename to log to
  * [A] _level_ - The loglevel to filter the logs. INFO,ERROR,DEBUG,WARN
  * [A] _logs_ - Defines the type of logging messages to log. One of the following: MAIN,SUBS,FIND,COMM,UPGRADE,IMDB,WEBSERVER,DB

## Work to do
Ther is still a lot of work to do to get this up and running.
Browse the github repository for all issues currently under consideration or review.

