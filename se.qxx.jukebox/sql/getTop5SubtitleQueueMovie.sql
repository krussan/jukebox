SELECT M.*
FROM Movie M
INNER JOIN SubtitleQueue SQ ON M._subtitleQueue_ID = SQ.ID
WHERE SQ.subtitleRetreiveResult = 0
LIMIT 5;
.quit
