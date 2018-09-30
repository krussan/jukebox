#!/bin/bash
PID=`cat jukebox.pid`

prog() {
    local w=40 p=$1;  shift
    # create a string of spaces, then change them to dots
    printf -v dots "%*s" "$(( $p*$w/100 ))" ""; dots=${dots// /.};
    # print those dots on a fixed-width space plus the percentage etc.
    printf "\r\e[K|%-*s| %s" "$w" "$dots" "$*";
}

echo Killing all ffmpeg processes
pkill ffprobe
pkill ffmpeg

if [ "$1" = "-p" ]; then
        echo Stopping jukebox with pid :: $PID
        kill $PID
else
        touch stopper.stp

        # test loop
        for x in {1..20} ; do
            d=`expr $x \* 5`
            prog "$d" Shutting down

            if [ ! -f stopper.stp ]; then
                echo "Server stopped successfully!"
                return
            fi
            sleep 1   # do some work here

        done;

        echo -e "\nShutdown FAILED !!!"
fi