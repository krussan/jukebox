PID=`cat jukebox.pid`

echo Stopping jukebox with pid :: $PID
kill $PID

