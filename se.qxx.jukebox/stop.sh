PID=`cat jukebox.pid`

echo Stopping jukebox with pid :: $PID
#kill $PID

echo Creating stopper file
touch stopper.stp

echo Killing all ffmpeg processes
pkill ffmpeg

