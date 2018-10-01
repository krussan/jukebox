#!/bin/bash

prog() {
    local w=40 p=$1;  shift
    # create a string of spaces, then change them to dots
    printf -v dots "%*s" "$(( $p*$w/100 ))" ""; dots=${dots// /.};
    # print those dots on a fixed-width space plus the percentage etc.
    printf "\r\e[K|%-*s| %s" "$w" "$dots" "$*";
}

killhard() {
   PID=`cat jukebox.pid`
   echo Stopping jukebox with pid :: $PID
   kill $PID
}

waitforshutdown() {
        for x in {1..20} ; do
            d=`expr $x \* 5`
            prog "$d" Shutting down

            if [ ! -f stopper.stp ]; then
                return 1
            fi
            sleep 1   # do some work here

        done;
        
        return 0
}

ffmpegkill() {
   echo Killing all ffmpeg processes
   pkill ffprobe
   pkill ffmpeg
}

ffmpegkill

if [ "$1" = "-p" ]; then
   killhard
else
   touch stopper.stp

   # test loop
   if waitforshutdown; then
      echo -e "\nShutdown FAILED !!!"
      echo "Killing processes hard ..."
      killhard
   else
      echo -e "\nServer stopped successfully!"
   fi
fi