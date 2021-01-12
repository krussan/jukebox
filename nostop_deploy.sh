#!/bin/bash
SERVER=$1
USER=$2
REMOTE_PATH=$3

echo $USER@$SERVER
echo $REMOTE_PATH

scp se.qxx.jukebox/build/outputs/* $USER@$SERVER:$REMOTE_PATH

# rm ${jukebox.deploy.server.path}/*.jar
# rm ${jukebox.deploy.server.path}/*.log

# <copy file="${base.dir}/JukeboxSettings.xml" todir="${build.dir}" />
# <copy file="${base.dir}/logging.properties" todir="${build.dir}" />
# <copy file="${base.dir}/imdb.xml" todir="${build.dir}" />
# <copy file="${base.dir}/parser.xml" todir="${build.dir}" />
# <copy file="${base.dir}/stop.sh" todir="${build.dir}" />

# scp ${build.dir}
# scp ${jukebox.lib.dir}


