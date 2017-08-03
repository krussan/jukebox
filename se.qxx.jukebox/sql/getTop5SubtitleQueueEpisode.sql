SELECT S.title, SS.seasonNumber, E.episodeNumber, SQ.subtitleRetreiveResult
FROM Series S
INNER JOIN SeriesSeason_Season SSS ON SSS._series_ID = S.ID
INNER JOIN Season SS ON SSS._season_ID = SS.ID
INNER JOIN SeasonEpisode_Episode SEE ON SEE._season_ID = S.ID
INNER JOIN Episode E ON SEE._episode_ID = E.ID
INNER JOIN SubtitleQueue SQ ON E._subtitleQueue_ID = SQ.ID
WHERE SQ.subtitleRetreiveResult = 0
LIMIT 5;
.quit
