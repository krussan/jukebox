PID=`cat jukebox.pid`

if [ "$1" = "-p" ]; then
	echo Stopping jukebox with pid :: $PID
	kill $PID
else
	touch stopper.stp
	
	sleep 5
fi

echo Killing all ffmpeg processes
pkill ffmpeg

