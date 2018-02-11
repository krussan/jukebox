#!/bin/bash
#BlobData               Movie                  Season_Genre         
#Episode                MovieMedia_Media       Series               
#EpisodeMedia_Media     Movie_Blacklist        SeriesSeason_Season  
#Episode_Genre          Movie_Genre            Series_Genre         
#Identifier             Rating                 Subtitle             
#Media                  Season                 SubtitleQueue        
#MediaSubtitle_Subs     SeasonEpisode_Episode  Version  
sqlite3 jukebox_proto.db ".dump Episode" > dump/Episode.sql
sed -i -e "s/CREATE\s*TABLE.*//gi" dump/Episode.sql
sed -i -e "s/PRAGMA.*//gi" dump/Episode.sql
sed -i -e "s/BEGIN\s*TRANSACTION.*//gi" dump/Episode.sql
sed -i -e "s/COMMIT.*//gi" dump/Episode.sql
sed -i -e "s/\"Episode\"/Episode/gi" dump/Episode.sql
