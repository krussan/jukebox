package se.qxx.jukebox.core;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;
import com.google.protobuf.Message;

import se.qxx.jukebox.core.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain;
import se.qxx.jukebox.domain.JukeboxDomain.Episode;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Season;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.domain.JukeboxDomain.SubtitleQueue;
import se.qxx.jukebox.domain.MovieOrSeries;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.interfaces.IDatabase;
import se.qxx.jukebox.interfaces.IJukeboxLogger;
import se.qxx.jukebox.interfaces.ISettings;
import se.qxx.protodb.DBType;
import se.qxx.protodb.ProtoDB;
import se.qxx.protodb.ProtoDBFactory;
import se.qxx.protodb.ProtoDBSort;
import se.qxx.protodb.SearchOptions;
import se.qxx.protodb.exceptions.DatabaseNotSupportedException;
import se.qxx.protodb.exceptions.IDFieldNotFoundException;
import se.qxx.protodb.exceptions.ProtoDBParserException;
import se.qxx.protodb.exceptions.SearchFieldNotFoundException;
import se.qxx.protodb.exceptions.SearchOptionsNotInitializedException;
import se.qxx.protodb.model.ProtoDBSearchOperator;

public class DB implements IDatabase {

	private static ReentrantLock lock = new ReentrantLock();
	
	private ISettings settings;
	private IJukeboxLogger mainLog;
	private IJukeboxLogger upgradeLog;
	private IJukeboxLogger findLog;
	private IJukeboxLogger dbLog;
	
	@Inject
	public DB(ISettings settings, LoggerFactory loggerFactory) {
		this.setSettings(settings);
		this.setDbLog(loggerFactory.create(LogType.DB));
		this.setMainLog(loggerFactory.create(LogType.MAIN));
		this.setUpgradeLog(loggerFactory.create(LogType.UPGRADE));
		this.setFindLog(loggerFactory.create(LogType.FIND));
	} 

	//---------------------------------------------------------------------------------------
	//------------------------------------------------------------------------ Get/Set
	//---------------------------------------------------------------------------------------

	public IJukeboxLogger getDbLog() {
		return dbLog;
	}

	public void setDbLog(IJukeboxLogger dbLog) {
		this.dbLog = dbLog;
	}

	public IJukeboxLogger getFindLog() {
		return findLog;
	}

	public void setFindLog(IJukeboxLogger findLog) {
		this.findLog = findLog;
	}

	public IJukeboxLogger getUpgradeLog() {
		return upgradeLog;
	}

	public void setUpgradeLog(IJukeboxLogger upgradeLog) {
		this.upgradeLog = upgradeLog;
	}

	public IJukeboxLogger getMainLog() {
		return mainLog;
	}

	public void setMainLog(IJukeboxLogger mainLog) {
		this.mainLog = mainLog;
	}

	public ISettings getSettings() {
		return settings;
	}

	public void setSettings(ISettings settings) {
		this.settings = settings;
	}

	//---------------------------------------------------------------------------------------
	//------------------------------------------------------------------------ Search
	//---------------------------------------------------------------------------------------

	@Override
	public List<Movie> searchMoviesByTitle(String searchString) {
		return searchMoviesByTitle(searchString, -1, -1);
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#searchMoviesByTitle(java.lang.String, int, int)
	 */
	@Override
	public List<Movie> searchMoviesByTitle(String searchString, int numberOfResults, int offset) {
		List<String> excludedObjects = new ArrayList<String>();
		excludedObjects.add("media.subs.textdata");
		excludedObjects.add("image");
		
		return searchByTitle(JukeboxDomain.Movie.getDefaultInstance()
				, "title"
				, searchString
				, numberOfResults
				, offset
				, excludedObjects
				, "identifiedTitle");
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#searchMoviesByID(int)
	 */
	@Override
	public Movie searchMoviesByID(int id) {
		try {
			List<String> excludedObjects = new ArrayList<String>();
			excludedObjects.add("media.subs.textdata");
			excludedObjects.add("image");
	
			ProtoDB db = getProtoDBInstance(true);
	
			try {
				if (db.getDBType() == DBType.Sqlite) lock.lock();

				List<Movie> result = db.search(
						SearchOptions.newBuilder(JukeboxDomain.Movie.getDefaultInstance())
							.addFieldName("ID")
							.addSearchArgument(id)
							.addOperator(ProtoDBSearchOperator.Equals)
							.setShallow(false)
							.addAllExcludedObjects(excludedObjects));				

					if (result.size() > 0)
						return result.get(0);
					else
						return null;

			}
			finally {
				if (db.getDBType() == DBType.Sqlite) lock.unlock();
			}
		
		}
		catch (Exception e) {
			this.getMainLog().Error("Failed to retrieve series listing from DB", e);
			return null;
		}		

	}
	

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#searchSeriesByTitle(java.lang.String)
	 */
	@Override
	public List<Series> searchSeriesByTitle(String searchString) {
		return searchSeriesByTitle(searchString, -1, -1);		
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#searchSeriesByTitle(java.lang.String, int, int)
	 */
	@Override
	public List<Series> searchSeriesByTitle(String searchString, int numberOfResults, int offset) {
		List<String> excludedObjects = new ArrayList<String>();
		excludedObjects.add("season"); // exclude underlying seasons
		excludedObjects.add("image"); // exclude full size image
		
		return searchByTitle(JukeboxDomain.Series.getDefaultInstance()
				, "title"
				, searchString
				, numberOfResults
				, offset
				, excludedObjects	
				, "title");
	}
	
	private <T extends Message> List<T> searchByTitle(
			T instance, 
			String searchField, 
			String searchString, 
			int numberOfResults,
			int offset,
			List<String> excludedObjects,
			String sortField) {
		try {
			ProtoDB db = getProtoDBInstance(true);

			try {
				if (db.getDBType() == DBType.Sqlite) lock.lock();

				if (StringUtils.isEmpty(searchString))
					searchField = "";
				else
					searchString = "%" + searchString + "%"; 
				
				return db.search(
					SearchOptions.newBuilder(instance)
						.addFieldName(searchField)
						.addSearchArgument(searchString)
						.addOperator(ProtoDBSearchOperator.Like)
						.setShallow(false)
						.setOffset(offset)
						.setNumberOfResults(numberOfResults)
						.setSortField(sortField)
						.setSortOrder(ProtoDBSort.Asc)
						.addAllExcludedObjects(excludedObjects));				


			}
			finally {
				if (db.getDBType() == DBType.Sqlite) lock.unlock();
				
			}
			
		}
		catch (Exception e) {
			this.getMainLog().Error("Failed to retrieve series listing from DB", e);
			return new ArrayList<T>();
		}		
	}


	private String replaceSearchString(String searchString) {
		String ret = searchString;
		ret = StringUtils.replace(ret, "%", "\\%");
		ret = StringUtils.replace(ret, "_", "\\_");
		return StringUtils.trim(ret);
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#findMovie(java.lang.String)
	 */
	@Override
	public Movie findMovie(String identifiedTitle) {
		String searchString = replaceSearchString(identifiedTitle);
		
		this.getFindLog().Debug(String.format("DB :: Series search string :: %s", searchString));
		 
		try {
			ProtoDB db = getProtoDBInstance();

			try {
				if (db.getDBType() == DBType.Sqlite) lock.lock();

				List<Movie> result =
						db.search(
							SearchOptions.newBuilder(JukeboxDomain.Movie.getDefaultInstance())
								.addFieldName("identifiedTitle") 
								.addSearchArgument(searchString)
								.addOperator(ProtoDBSearchOperator.Like));
					
				if (result.size() > 0)
					return result.get(0);
				else 
					return null;				

			}
			finally {
				if (db.getDBType() == DBType.Sqlite) lock.unlock();				
			}

			
		} catch (Exception e) {
			this.getFindLog().Error("failed to get information from database", e);
//			Log.Debug(String.format("Failing query was ::\n\t%s", statement), LogType.MAIN);
			
			return null;
		}
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#findSeries(java.lang.String)
	 */
	@Override
	public Series findSeries(String identifiedTitle) {
		String searchString = replaceSearchString(identifiedTitle) + "%";
		
		this.getMainLog().Debug(String.format("DB :: Series search string :: %s", searchString));
		 
		try {
			ProtoDB db = getProtoDBInstance();
			
			try {
				if (db.getDBType() == DBType.Sqlite) lock.lock();

				List<Series> result =
						db.search(
							SearchOptions.newBuilder(JukeboxDomain.Series.getDefaultInstance())
								.addFieldName("identifiedTitle") 
								.addSearchArgument(searchString)
								.addOperator(ProtoDBSearchOperator.Like));

				if (result.size() > 0)
					return result.get(0);
				else 
					return null;

			}
			finally {
				if (db.getDBType() == DBType.Sqlite) lock.unlock();
				
			}
			
		} catch (Exception e) {
			this.getMainLog().Error("failed to get information from database", e);
//			Log.Debug(String.format("Failing query was ::\n\t%s", statement), LogType.MAIN);
			
			return null;
		}
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#searchSeriesById(int)
	 */
	@Override
	public Series searchSeriesById(int id) {
		try {
			ProtoDB db = getProtoDBInstance();
			
			try {
				if (db.getDBType() == DBType.Sqlite) lock.lock();

				List<Series> result = 
						db.search(SearchOptions.newBuilder(JukeboxDomain.Series.getDefaultInstance()) 
							.addFieldName("ID") 
							.addSearchArgument(id) 
							.addOperator(ProtoDBSearchOperator.Equals)
							.addExcludedObject("season.episode")
							.addExcludedObject("image")
							.addExcludedObject("season.image"));
						

					if (result.size() > 0)
						return result.get(0);
					else
						return null;

			}
			finally {
				if (db.getDBType() == DBType.Sqlite) lock.unlock();
				
			}
			
		} catch (Exception e) {
			this.getMainLog().Error("failed to get information from database", e);
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#searchSeasonById(int)
	 */
	@Override
	public Season searchSeasonById(int id) {
		try {
			ProtoDB db = getProtoDBInstance();
			
			try {
				if (db.getDBType() == DBType.Sqlite) lock.lock();

				List<Season> result = 
						db.search(SearchOptions.newBuilder(JukeboxDomain.Season.getDefaultInstance()) 
							.addFieldName("ID") 
							.addSearchArgument(id) 
							.addOperator(ProtoDBSearchOperator.Equals)
							.addExcludedObject("image")
							.addExcludedObject("episode.image")
							.addExcludedObject("episode.media.subs.textdata"));
					
						

					if (result.size() > 0)
						return result.get(0);
					else
						return null;

			}
			finally {
				if (db.getDBType() == DBType.Sqlite) lock.unlock();
				
			}
			
		} catch (Exception e) {
			this.getMainLog().Error("failed to get information from database", e);
		}
		
		return null;
	}

	//---------------------------------------------------------------------------------------
	//------------------------------------------------------------------------ Get
	//---------------------------------------------------------------------------------------

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#getMovie(int)
	 */
	@Override
	public Movie getMovie(int id) {
		try {
			ProtoDB db = getProtoDBInstance();
			
			try {
				if (db.getDBType() == DBType.Sqlite) lock.lock();

				return db.get(
						id, 
						Movie.getDefaultInstance());

			}
			finally {
				if (db.getDBType() == DBType.Sqlite) lock.unlock();
				
			}
			
		} catch (Exception e) {
			this.getMainLog().Error("failed to get information from database", e);
			
			
		}
		
		return null;
	}

	


	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#getSeries(int)
	 */
	@Override
	public Series getSeries(int id) {
		try {
			ProtoDB db = getProtoDBInstance();
			
			try {
				if (db.getDBType() == DBType.Sqlite) lock.lock();

				List<Series> result = 
						db.search(SearchOptions.newBuilder(JukeboxDomain.Series.getDefaultInstance()) 
							.addFieldName("ID") 
							.addSearchArgument(id) 
							.addOperator(ProtoDBSearchOperator.Equals));

					if (result.size() > 0)
						return result.get(0);
					else
						return null;

			}
			finally {
				if (db.getDBType() == DBType.Sqlite) lock.unlock();
				
			}
			
		} catch (Exception e) {
			this.getMainLog().Error("failed to get information from database", e);
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#getSeason(int)
	 */
	@Override
	public Season getSeason(int id) {
		try {
			ProtoDB db = getProtoDBInstance();
			try {
				if (db.getDBType() == DBType.Sqlite) lock.lock();

				List<Season> result =
						db.search(SearchOptions.newBuilder(JukeboxDomain.Season.getDefaultInstance()) 
								.addFieldName("ID") 
								.addSearchArgument(id) 
								.addOperator(ProtoDBSearchOperator.Equals));

				if (result.size() > 0)
					return result.get(0);
				else
					return null;

			}
			finally {
				if (db.getDBType() == DBType.Sqlite) lock.unlock();
				
			}
			
		} catch (Exception e) {
			this.getMainLog().Error("failed to get information from database", e);
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#getEpisode(int)
	 */
	@Override
	public Episode getEpisode(int id) {
		try {
			ProtoDB db = getProtoDBInstance();
			
			try {
				if (db.getDBType() == DBType.Sqlite) lock.lock();

				return db.get(id, Episode.getDefaultInstance());
			}
			finally {
				if (db.getDBType() == DBType.Sqlite) lock.unlock();
				
			}
			
		} catch (Exception e) {
			this.getMainLog().Error("failed to get information from database", e);
		}
		
		return null;
	}

	//---------------------------------------------------------------------------------------
	//------------------------------------------------------------------------ Save
	//---------------------------------------------------------------------------------------

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#save(se.qxx.jukebox.domain.JukeboxDomain.Movie)
	 */
	@Override
	public Movie save(Movie m) {
		try {
			ProtoDB db = getProtoDBInstance();
			
			try {
				if (db.getDBType() == DBType.Sqlite) lock.lock();
				
				return db.save(m);
			}
			finally {
				if (db.getDBType() == DBType.Sqlite) lock.unlock();				
			}
			
		}
		catch (Exception e) {
			this.getMainLog().Error("Failed to store movie to DB", e);
			
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#save(se.qxx.jukebox.domain.JukeboxDomain.Media)
	 */
	@Override
	public Media save(Media md) {
		try {
			ProtoDB db = getProtoDBInstance();
			
			try {
				if (db.getDBType() == DBType.Sqlite) lock.lock();
				
				return db.save(md);
			}
			finally {
				if (db.getDBType() == DBType.Sqlite) lock.unlock();				
			}
		}
		catch (Exception e) {
			this.getMainLog().Error("Failed to store media to DB", e);
			
			return null;
		}
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#save(se.qxx.jukebox.domain.JukeboxDomain.Episode)
	 */
	@Override
	public Episode save(Episode episode) {
		try {
			ProtoDB db = getProtoDBInstance();

			try {
				if (db.getDBType() == DBType.Sqlite) lock.lock();
				
				return db.save(episode);
			}
			finally {
				if (db.getDBType() == DBType.Sqlite) lock.unlock();
			}
		}
		catch (Exception e) {
			this.getMainLog().Error("Failed to store episode to DB", e);
			
			return null;
		}
	}	

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#save(se.qxx.jukebox.domain.JukeboxDomain.Series)
	 */
	@Override
	public Series save(Series series) {
		try {
			ProtoDB db = getProtoDBInstance();
			
			try {
				if (db.getDBType() == DBType.Sqlite) lock.lock();
				
				return db.save(series);
			}
			finally {
				if (db.getDBType() == DBType.Sqlite) lock.unlock();
			}
		}
		catch (Exception e) {
			this.getMainLog().Error("Failed to store episode to DB", e);
			
			return null;
		}
	}	

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#saveAsync(se.qxx.jukebox.domain.JukeboxDomain.Series)
	 */
	@Override
	public void saveAsync(Series series) {
		Thread t = new Thread(() -> {
			try {

				ProtoDB db = getProtoDBInstance();
				
				try {
					if (db.getDBType() == DBType.Sqlite) lock.lock();
					
					db.save(series);
				}
				finally {
					if (db.getDBType() == DBType.Sqlite) lock.unlock();
					
				}
			}
			catch (Exception e) {
				this.getMainLog().Error("Failed to store episode to DB", e);
			}

		});
		t.start();
	}	


	//---------------------------------------------------------------------------------------
	//------------------------------------------------------------------------ Delete
	//---------------------------------------------------------------------------------------
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#delete(se.qxx.jukebox.domain.JukeboxDomain.Movie)
	 */
	@Override
	public void delete(Movie m) throws ClassNotFoundException, SQLException, DatabaseNotSupportedException {
		try {
			ProtoDB db = getProtoDBInstance();
			
			try {
				if (db.getDBType() == DBType.Sqlite) lock.lock();
				
				db.delete(m);
			}
			finally {
				if (db.getDBType() == DBType.Sqlite) lock.unlock();
				
			}

		}
		catch (ClassNotFoundException | SQLException | DatabaseNotSupportedException e) {
			this.getMainLog().Error("Failed to delete movie in DB", e);
			
			throw e;
		}		
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#delete(se.qxx.jukebox.domain.JukeboxDomain.Series)
	 */
	@Override
	public void delete(Series s) throws ClassNotFoundException, SQLException, DatabaseNotSupportedException  {
		try {
			ProtoDB db = getProtoDBInstance();
			
			try {
				if (db.getDBType() == DBType.Sqlite) lock.lock();
				
				db.delete(s);
			}
			finally {
				if (db.getDBType() == DBType.Sqlite) lock.unlock();
				
			}
			
		}
		catch (ClassNotFoundException | SQLException | DatabaseNotSupportedException e) {
			this.getMainLog().Error("Failed to delete series in DB", e);
			
			throw e;
		}			
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#delete(se.qxx.jukebox.domain.JukeboxDomain.Season)
	 */
	@Override
	public void delete(Season sn) throws ClassNotFoundException, SQLException,  DatabaseNotSupportedException  {
		try {
			ProtoDB db = getProtoDBInstance();
			
			try {
				if (db.getDBType() == DBType.Sqlite) lock.lock();
				
				db.delete(sn);
			}
			finally {
				if (db.getDBType() == DBType.Sqlite) lock.unlock();
			}
		}
		catch (ClassNotFoundException | SQLException |  DatabaseNotSupportedException  e) {
			this.getMainLog().Error("Failed to delete season in DB", e);
			
			throw e;
		}			
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#delete(se.qxx.jukebox.domain.JukeboxDomain.Episode)
	 */
	@Override
	public void delete(Episode ep) throws ClassNotFoundException, SQLException, DatabaseNotSupportedException  {
		try {
			ProtoDB db = getProtoDBInstance();
			
			try {				
				if (db.getDBType() == DBType.Sqlite) lock.lock();
				db.delete(ep);
			}
			finally {
				if (db.getDBType() == DBType.Sqlite) lock.unlock();				
			}

		}
		catch (ClassNotFoundException | SQLException | DatabaseNotSupportedException  e) {
			this.getMainLog().Error("Failed to delete episode in DB", e);
			
			throw e;
		}				
	}

	//---------------------------------------------------------------------------------------
	//------------------------------------------------------------------------ Watched
	//---------------------------------------------------------------------------------------
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#toggleWatched(se.qxx.jukebox.domain.JukeboxDomain.Movie)
	 */
	@Override
	public void toggleWatched(Movie m) {
		try {
			ProtoDB db = getProtoDBInstance();
			
			try {
				if (db.getDBType() == DBType.Sqlite) lock.lock();
				
				db.save(Movie.newBuilder(m).setWatched(!m.getWatched()).build());
			}
			finally {
				if (db.getDBType() == DBType.Sqlite) lock.unlock();
			}
		}
		catch (Exception e) {
			this.getMainLog().Error("Failed to store movie to DB", e);
		}

	}		

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#getConverterQueue()
	 */
	@Override
	public List<Media> getConverterQueue() {
		List<Media> result = new ArrayList<Media>();
       try {
           ProtoDB db = getProtoDBInstance();
               
			try {
				if (db.getDBType() == DBType.Sqlite) lock.lock();

               // Restrict result to 5.
    	   	   // should only have the queue state Queued if download completed
               result = db.search(
        		   SearchOptions.newBuilder(JukeboxDomain.Media.getDefaultInstance())
        		   	.addFieldName("converterState")
        		   	.addOperator(ProtoDBSearchOperator.Equals)
        		   	.addSearchArgument("Queued")
        		   	.setNumberOfResults(5)
        		   	.setOffset(0));

			}
			finally {
				if (db.getDBType() == DBType.Sqlite) lock.unlock();
				
			}
       }
       catch (Exception e) {
           this.getMainLog().Error("Failed to retrieve movie listing from DB", e);
       }
       
       return result;
	
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#setDownloadCompleted(int)
	 */
	@Override
	public void setDownloadCompleted(int id) {
		try {
			ProtoDB db = getProtoDBInstance();
			
			try {
				if (db.getDBType() == DBType.Sqlite) lock.lock();

				String sql = String.format("UPDATE Media SET downloadcomplete = 1 WHERE ID = %s", id);
				db.executeNonQuery(sql);

			}
			finally {
				if (db.getDBType() == DBType.Sqlite) lock.unlock();
				
			}
		} catch (Exception e) {
			this.getMainLog().Error(String.format("Failed to set download complete"), e);
		}				
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#cleanupConverterQueue()
	 */
	@Override
	public  void cleanupConverterQueue() {
		try {
			ProtoDB db = getProtoDBInstance();
			
			try {
				if (db.getDBType() == DBType.Sqlite) lock.lock();

				String sql = "UPDATE Media SET _converterstate_ID = 2 WHERE _converterstate_ID IN (4,5)";
				db.executeNonQuery(sql);
			}
			finally {
				if (db.getDBType() == DBType.Sqlite) lock.unlock();
			}

		} catch (Exception e) {
			this.getMainLog().Error(String.format("Failed to clean subtitle queue"), e);
		}		
	}



	//---------------------------------------------------------------------------------------
	//------------------------------------------------------------------------ Subtitles
	//---------------------------------------------------------------------------------------

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#addMovieToSubtitleQueue(se.qxx.jukebox.domain.JukeboxDomain.Movie)
	 */
	@Override
	public Movie addMovieToSubtitleQueue(Movie m) {
		try {
			ProtoDB db = getProtoDBInstance();
			int id = m.hasSubtitleQueue() ? m.getSubtitleQueue().getID() : -1;
			
			m = Movie.newBuilder(m).setSubtitleQueue(
				SubtitleQueue.newBuilder()
					.setID(id)
					.setSubtitleRetreiveResult(0)
					.setSubtitleQueuedAt(getCurrentUnixTimestamp())
					.build())
				.build();
			
			try {
				if (db.getDBType() == DBType.Sqlite) lock.lock();
				return db.save(m);
			}
			finally {
				if (db.getDBType() == DBType.Sqlite) lock.unlock();
			}
		}
		catch (Exception e) {
			this.getMainLog().Error("Failed to store movie to DB", e);
		}
		return m;
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#addEpisodeToSubtitleQueue(se.qxx.jukebox.domain.JukeboxDomain.Episode)
	 */
	@Override
	public Episode addEpisodeToSubtitleQueue(Episode e) {
		try {
			ProtoDB db = getProtoDBInstance();
			
			e = Episode.newBuilder(e).setSubtitleQueue(
					SubtitleQueue.newBuilder()
						.setID(-1)
						.setSubtitleRetreiveResult(0)				
						.setSubtitleQueuedAt(getCurrentUnixTimestamp())
						.build())
					.build();
			
			try {
				if (db.getDBType() == DBType.Sqlite) lock.lock();
				return db.save(e);
			}
			finally {
				if (db.getDBType() == DBType.Sqlite) lock.unlock();
			}
		}
		catch (Exception ex) {
			this.getMainLog().Error("Failed to store epsiode to DB", ex);
		}
		return e;
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#getCurrentUnixTimestamp()
	 */
	@Override
	public long getCurrentUnixTimestamp() {
		return System.currentTimeMillis() / 1000L;
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#getSubtitleQueue()
	 */
	@Override
	public List<MovieOrSeries> getSubtitleQueue() {
		List<MovieOrSeries> result = new ArrayList<MovieOrSeries>();
		List<Movie> movies = new ArrayList<Movie>();
		List<Series> series = new ArrayList<Series>();
		
		try {
			ProtoDB db = getProtoDBInstance();
			
			try {
				if (db.getDBType() == DBType.Sqlite) lock.lock();

				// Restrict result to 5. Since the list will be retrieved again it does not matter.
				movies =
					db.search(
						SearchOptions.newBuilder(JukeboxDomain.Movie.getDefaultInstance())
						.addFieldName("subtitleQueue.subtitleRetreiveResult")
						.addSearchArgument(0)
						.addOperator(ProtoDBSearchOperator.Equals)
						.setNumberOfResults(5)
						.addExcludedObject("media.subs")
						.setOffset(0));
	
				// this is a bit dangerous.
				// what if we cut a series/season in half and save the series (!)
				// So to be sure we save _only_ the episode from the SubtitleDownloader.
				series =
					db.search(
						SearchOptions.newBuilder(JukeboxDomain.Series.getDefaultInstance())
						.addFieldName("season.episode.subtitleQueue.subtitleRetreiveResult")
						.addSearchArgument(0)
						.addOperator(ProtoDBSearchOperator.Equals)
						.setNumberOfResults(5)
						.addExcludedObject("season.episode.media.subs")
						.setOffset(0));
	

			}
			finally {
				if (db.getDBType() == DBType.Sqlite) lock.unlock();
			}

			result = constructSubtitleQueue(movies, series);
		}
		catch (Exception e) {
			this.getMainLog().Error("Failed to retrieve movie listing from DB", e);
		}
		
		return result;
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#getProtoDBInstance()
	 */
	@Override
	public ProtoDB getProtoDBInstance() throws DatabaseNotSupportedException {
		return getProtoDBInstance(true);
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#getProtoDBInstance(boolean)
	 */
	@Override
	public ProtoDB getProtoDBInstance(boolean populateBlobs) throws DatabaseNotSupportedException {
		String driver = this.getSettings().getSettings().getDatabase().getDriver();
		String connectionString = this.getSettings().getSettings().getDatabase().getConnectionString();

		return getProtoDBInstance(driver, connectionString, populateBlobs);
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#getProtoDBInstance(java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public ProtoDB getProtoDBInstance(String driver, String connectionString, boolean populateBlobs) throws DatabaseNotSupportedException {
		ProtoDB db = null; 
		String logFilename = this.getDbLog().getLoggerFilename();
	
		if (StringUtils.isEmpty(logFilename))
			db = ProtoDBFactory.getInstance(driver, connectionString);
		else
			db = ProtoDBFactory.getInstance(driver, connectionString, logFilename);
		
		db.setPopulateBlobs(populateBlobs);
		return db;
	}

	private List<MovieOrSeries> constructSubtitleQueue(List<Movie> movies, List<Series> series) {
		List<MovieOrSeries> moss = new ArrayList<MovieOrSeries>();
		
		for (Movie m : movies) {
			MovieOrSeries mos = new MovieOrSeries(m);
			moss.add(mos);
		}
		
		// we need to create a single Series object for every episode
		// since the MovieOrSeries object is mainly used for identifying
		moss.addAll(decoupleSeries(series));
		
		return moss;
	}

	private List<MovieOrSeries> decoupleSeries(List<Series> series) {
		List<MovieOrSeries> moss = new ArrayList<MovieOrSeries>();
		
		for (Series s : series) {
			for (Season ss : s.getSeasonList()) {
				for (Episode e : ss.getEpisodeList()) {
					Series s2 = Series.newBuilder(s)
							.clearSeason()
							
							.addSeason(Season.newBuilder(ss)
								.clearEpisode()
								.addEpisode(e)
								.build())
					
							.build();
					
					moss.add(new MovieOrSeries(s2));
				}
			}
		}
		
		return moss;
	}
	
	//---------------------------------------------------------------------------------------
	//------------------------------------------------------------------------ Blacklist
	//---------------------------------------------------------------------------------------

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#addToBlacklist(se.qxx.jukebox.domain.JukeboxDomain.Movie)
	 */
	@Override
	public void addToBlacklist(Movie m) {
		try {
			ProtoDB db = getProtoDBInstance();
			m = Movie.newBuilder(m).addBlacklist(m.getImdbId()).build();			
			
			try {
				if (db.getDBType() == DBType.Sqlite) lock.lock();
				db.save(m);
			}
			finally {
				if (db.getDBType() == DBType.Sqlite) lock.unlock();
			}
		}
		catch (Exception e) {
			this.getMainLog().Error("Failed to add movie to blacklist in DB", e);
		}		
	}

	
	//---------------------------------------------------------------------------------------
	//------------------------------------------------------------------------ Version
	//---------------------------------------------------------------------------------------
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#getVersion()
	 */
	@Override
	public Version getVersion() throws ClassNotFoundException {
		Connection conn = null;
		int minor = 0;
		int major = 0;
		
		try {
			ProtoDB db = getProtoDBInstance();
			conn = db.getConnection();
			
			try {
				if (db.getDBType() == DBType.Sqlite) lock.lock();

				PreparedStatement prep = conn.prepareStatement("SELECT minor, major FROM Version WHERE ID=1");
				ResultSet rs = prep.executeQuery();
				if (rs.next()) {
					minor = rs.getInt("minor");
					major = rs.getInt("major");
				}

			}
			finally {
				if (db.getDBType() == DBType.Sqlite) lock.unlock();
			}

		} catch (Exception e) {
			minor = 0;
			major = 0;
		}
		
		finally {
			disconnect(conn);
		}
		
		//default for pre-protodb is 0.10
		if (minor == 0 && major == 0)
			minor = 10;
		
		return new Version(major, minor);
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#setVersion(se.qxx.jukebox.Version)
	 */
	@Override
	public  void setVersion(Version ver) throws ClassNotFoundException, SQLException, DatabaseNotSupportedException {
		Connection conn = null;
		try {
			ProtoDB db = getProtoDBInstance();
			conn = db.getConnection();

			try {
				if (db.getDBType() == DBType.Sqlite) lock.lock();

				PreparedStatement prep = conn.prepareStatement("UPDATE Version SET major = ?, minor= ? WHERE ID = 1");
				prep.setInt(1, ver.getMajor());
				prep.setInt(2, ver.getMinor());
				
				prep.execute();

			}
			finally {
				if (db.getDBType() == DBType.Sqlite) lock.unlock();
			}
		}
		finally {
			disconnect(conn);
		}
	}
	
	private void insertVersion(Version ver) throws ClassNotFoundException, SQLException, DatabaseNotSupportedException {
		Connection conn = null;
		try {
			ProtoDB db = getProtoDBInstance();
			conn = db.getConnection();
			
			try {
				if (db.getDBType() == DBType.Sqlite) lock.lock();

				PreparedStatement prep = conn.prepareStatement("INSERT INTO Version (major, minor) VALUES (?, ?)");
				prep.setInt(1, ver.getMajor());
				prep.setInt(2, ver.getMinor());
				
				prep.execute();

			}
			finally {
				if (db.getDBType() == DBType.Sqlite) lock.unlock();				
			}

		}
		finally {
			disconnect(conn);
		}
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#backup()
	 */
	@Override
	public String backup() throws IOException, DatabaseNotSupportedException  {
		String backupFilename = StringUtils.EMPTY;
		
		ProtoDB db = getProtoDBInstance();
		if (db.getDBType() == DBType.Sqlite) {
			String[] splitted = StringUtils.split(db.getDatabaseBackend().getConnectionString(), ':');
			ArrayUtils.reverse(splitted);
			String databaseFilename = splitted[0];
			
			backupFilename = String.format("%s.bak", databaseFilename);
			File backup = new File(backupFilename);
			File current = new File(databaseFilename);
			
			int i = 1;
			while (backup.exists()) {
				backupFilename = String.format("%s.bak.%s", databaseFilename, i);
				backup = new File(backupFilename);
				i++;
			}
			
			System.out.println(String.format("Making backup to :: %s", backupFilename));
			FileUtils.copyFile(current, backup);
		}
		else {
			System.out.println("Database is not Sqlite. Backup not available. Send a prayer to the data gods.");
		}
		return backupFilename;
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#restore(java.lang.String)
	 */
	@Override
	public void restore(String backupFilename) throws IOException {
		File backup = new File(backupFilename);
		File restoreFile = new File("jukebox_proto.db");
		
		if (restoreFile.exists())
			restoreFile.delete();
		
		FileUtils.copyFile(backup, restoreFile);
	}
	

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#executeUpgradeStatements(java.lang.String[])
	 */
	@Override
	public boolean executeUpgradeStatements(String[] statements) {
		Connection conn = null;
		String sql = StringUtils.EMPTY;
		
		try {
			lock.lock();
			conn = initialize();
			conn.setAutoCommit(false);

			int nrOfScripts = statements.length;
			for (int i=0; i<statements.length;i++) {
				sql = statements[i];
				System.out.println(String.format("Running script\t\t[%s/%s]", i + 1, nrOfScripts));

				PreparedStatement prep = conn.prepareStatement(sql);			
				prep.execute();
			}

			conn.commit();
			
			return true;
		}
				
		catch (Exception e) {
			this.getUpgradeLog().Error("Upgrade failed", e);
			this.getUpgradeLog().Debug("Failing query was::");
			this.getUpgradeLog().Debug(sql);
			
			try {
				conn.rollback();
			} catch (SQLException sqlEx) {}
			
			return false;
		}
		finally {
			disconnect(conn);
			lock.unlock();
		}
	}
	
	private Connection initialize() throws ClassNotFoundException, SQLException, DatabaseNotSupportedException {
		return getProtoDBInstance().getConnection();
	}
	
	private void disconnect(Connection conn) {
		try {
			if (conn != null)
				conn.close();
		} catch (SQLException e) {
		}
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#purgeDatabase()
	 */
	@Override
	public boolean purgeDatabase() {
		try {
			lock.lock();
			
			getProtoDBInstance().dropAllTables();
		
			setupDatabase();
			
			return true;
		}
		catch (Exception e) {
			this.getMainLog().Error("Purge failed", e);
			return false;
		}
		finally {
			lock.unlock();
		}
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#setupDatabase()
	 */
	@Override
	public boolean setupDatabase() throws ClassNotFoundException, SQLException, IDFieldNotFoundException, DatabaseNotSupportedException {
		try {
			lock.lock();
			Version ver = getVersion();
			if (ver.getMajor() == 0 && ver.getMinor() == 10) {
				ProtoDB db = getProtoDBInstance();
				
				db.setupDatabase(Movie.getDefaultInstance());
				db.setupDatabase(se.qxx.jukebox.domain.JukeboxDomain.Version.getDefaultInstance());
				db.setupDatabase(Series.getDefaultInstance());
				insertVersion(new Version());
				
				return false;
			}
		} catch (ClassNotFoundException | SQLException
				| IDFieldNotFoundException | DatabaseNotSupportedException e) {
			this.getMainLog().Error("Failed to setup database", e);
			
			throw e;
		}
		finally {
			lock.unlock();
		}
		
		return true;
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#getMovieByMediaID(int)
	 */
	@Override
	public Movie getMovieByMediaID(int mediaID) {
		try {
			ProtoDB db = getProtoDBInstance();
			
			try {
				if (db.getDBType() == DBType.Sqlite) lock.lock();

				List<Movie> result =
						db.search(
							SearchOptions.newBuilder(JukeboxDomain.Movie.getDefaultInstance())
							.addFieldName("media.ID")
							.addSearchArgument(mediaID)
							.addOperator(ProtoDBSearchOperator.Equals));
					
					if (result.size() > 0)
						return result.get(0);
					else 
						return null;

			}
			finally {
				if (db.getDBType() == DBType.Sqlite) lock.unlock();
			}
			
		} catch (Exception e) {
			this.getMainLog().Error("failed to get information from database", e);
//			Log.Debug(String.format("Failing query was ::\n\t%s", statement), LogType.MAIN);
			
			return null;
		}
			
	}

	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#getMediaByFilename(java.lang.String)
	 */
	@Override
	public Media getMediaByFilename(String filename) {
		try {
			ProtoDB db = getProtoDBInstance();

			try {
				if (db.getDBType() == DBType.Sqlite) lock.lock();

				List<Media> result =
						db.search(
							SearchOptions.newBuilder(JukeboxDomain.Media.getDefaultInstance())
							.addFieldName("filename")
							.addSearchArgument(filename)
							.addOperator(ProtoDBSearchOperator.Equals)
							.setShallow(false)
							.addExcludedObject("subs"));
					
				if (result.size() > 0)
					return result.get(0);
				else 
					return null;

			}
			finally {
				if (db.getDBType() == DBType.Sqlite) lock.unlock();
			}
			
		} catch (Exception e) {
			this.getMainLog().Error("failed to get information from database", e);
//			Log.Debug(String.format("Failing query was ::\n\t%s", statement), LogType.MAIN);
			
			return null;
		}
			
	}
	
	public Media getMediaByStartOfFilename(String startOfFilename) {
		try {
			ProtoDB db = getProtoDBInstance();

			try {
				if (db.getDBType() == DBType.Sqlite) lock.lock();

				List<Media> result =
						db.search(
							SearchOptions.newBuilder(JukeboxDomain.Media.getDefaultInstance())
							.addFieldName("filename")
							.addSearchArgument(startOfFilename + "%")
							.addOperator(ProtoDBSearchOperator.Like)
							.setShallow(false)
							.addExcludedObject("subs"));
					
				if (result.size() > 0)
					return result.get(0);
				else 
					return null;

			}
			finally {
				if (db.getDBType() == DBType.Sqlite) lock.unlock();
			}
			
		} catch (Exception e) {
			this.getMainLog().Error("failed to get information from database", e);
//			Log.Debug(String.format("Failing query was ::\n\t%s", statement), LogType.MAIN);
			
			return null;
		}		
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#getMediaById(int)
	 */
	@Override
	public Media getMediaById(int mediaId)  {
		try {
			ProtoDB db = getProtoDBInstance();

			try {
				if (db.getDBType() == DBType.Sqlite) lock.lock();

				return db.get(mediaId, Media.getDefaultInstance());
			}
			finally {
				if (db.getDBType() == DBType.Sqlite) lock.unlock();
			}
		} catch (ClassNotFoundException | SQLException | DatabaseNotSupportedException e) {
			this.getMainLog().Error(String.format("Failed to get media %s", mediaId), e);
		}
		
		return null;		
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#getMovieBySubfilename(java.lang.String)
	 */
	@Override
	public Movie getMovieBySubfilename(String subsFilename) throws DatabaseNotSupportedException {
		try {
			String searchString = replaceSearchString(subsFilename) + "%";
			
			ProtoDB db = getProtoDBInstance();

			try {
				if (db.getDBType() == DBType.Sqlite) lock.lock();

				List<Movie> result = 
						db.search(
							SearchOptions.newBuilder(JukeboxDomain.Movie.getDefaultInstance())
							.addFieldName("media.subs.filename")
							.addSearchArgument(searchString)
							.addOperator(ProtoDBSearchOperator.Like));
					
				if (result.size() > 0)
					return result.get(0);
			}
			finally {
				if (db.getDBType() == DBType.Sqlite) lock.unlock();
			}
			
		} catch (ClassNotFoundException | SQLException | SearchFieldNotFoundException | ProtoDBParserException | SearchOptionsNotInitializedException e) {
			this.getMainLog().Error(String.format("Failed to get movie with subs filename %s", subsFilename), e);
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#purgeSeries()
	 */
	@Override
	public void purgeSeries() {
		try {
			ProtoDB db = getProtoDBInstance();

			try {
				if (db.getDBType() == DBType.Sqlite) lock.lock();

				List<Series> result =
						db.search(SearchOptions.newBuilder(JukeboxDomain.Series.getDefaultInstance())
								.addFieldName("title")
								.addSearchArgument("%")
								.addOperator(ProtoDBSearchOperator.Like));
					
				for (Series s : result) {
					System.out.println(String.format("Purging :: %s", s.getTitle()));
					db.delete(s);
				}

			}
			finally {
				if (db.getDBType() == DBType.Sqlite) lock.unlock();
			}
			
		} catch (ClassNotFoundException | SQLException | SearchFieldNotFoundException | DatabaseNotSupportedException | ProtoDBParserException | SearchOptionsNotInitializedException e) {
			this.getMainLog().Error(String.format("Failed to purge series"), e);
		}		
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#cleanSubtitleQueue()
	 */
	@Override
	public  void cleanSubtitleQueue() {
		try {
			ProtoDB db = getProtoDBInstance();

			try {
				if (db.getDBType() == DBType.Sqlite) lock.lock();

				String sql = "UPDATE SubtitleQueue SET subtitleRetreiveResult = -2 WHERE ID NOT IN (SELECT _subtitleQueue_ID FROM Movie) AND ID NOT IN (SELECT _subtitleQueue_ID FROM Episode);";
				db.executeNonQuery(sql);
			}
			finally {
				if (db.getDBType() == DBType.Sqlite) lock.unlock();
			}

		} catch (Exception e) {
			this.getMainLog().Error(String.format("Failed to clean subtitle queue"), e);
		}		
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#getTotalNrOfMovies()
	 */
	@Override
	public int getTotalNrOfMovies() {
		try {
			ProtoDB db = getProtoDBInstance();

			try {
				if (db.getDBType() == DBType.Sqlite) lock.lock();
				
				String sql = "SELECT COUNT(1) FROM Movie;";
				return db.executeScalar(sql);
			}
			finally {
				if (db.getDBType() == DBType.Sqlite) lock.unlock();
				
			}

		} catch (Exception e) {
			this.getMainLog().Error(String.format("Failed to clean subtitle queue"), e);
		}		
		
		return 0;
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#getTotalNrOfSeries()
	 */
	@Override
	public int getTotalNrOfSeries() {
		try {
			ProtoDB db = getProtoDBInstance();
			
			try {
				if (db.getDBType() == DBType.Sqlite) lock.lock();

				String sql = "SELECT COUNT(1) FROM Series;";
				return db.executeScalar(sql);
			}
			finally {
				if (db.getDBType() == DBType.Sqlite) lock.unlock();
				
			}

		} catch (Exception e) {
			this.getMainLog().Error(String.format("Failed to clean subtitle queue"), e);
		}		
		
		return 0;
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#getTotalNrOfSeasons(int)
	 */
	@Override
	public int getTotalNrOfSeasons(int seriesID) {
		try {
			ProtoDB db = getProtoDBInstance();

			try {
				if (db.getDBType() == DBType.Sqlite) lock.lock();

				String sql = String.format("SELECT COUNT(1) FROM SeriesSeason_Season SS INNER JOIN Season S ON SS._season_ID = S.ID WHERE _series_ID = %s;", seriesID);
				return db.executeScalar(sql);
			}
			finally {
				if (db.getDBType() == DBType.Sqlite) lock.unlock();
				
			}

		} catch (Exception e) {
			this.getMainLog().Error(String.format("Failed to clean subtitle queue"), e);
		}		
		
		return 0;		
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#getTotalNrOfEpisodes(int)
	 */
	@Override
	public int getTotalNrOfEpisodes(int seasonID) {
		try {
			ProtoDB db = getProtoDBInstance();
			
			try {
				if (db.getDBType() == DBType.Sqlite) lock.lock();

				String sql = String.format("SELECT COUNT(1) FROM SeasonEpisode_Episode SS INNER JOIN Episode E ON SS._episode_ID = E.ID WHERE _season_ID = %s;", seasonID);
				return db.executeScalar(sql);
			}
			finally {
				if (db.getDBType() == DBType.Sqlite) lock.unlock();
			}

		} catch (Exception e) {
			this.getMainLog().Error(String.format("Failed to clean subtitle queue"), e);
		}		
		
		return 0;				
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#saveConversion(int, java.lang.String, int)
	 */
	@Override
	public void saveConversion(int id, String newFilename, int value) {
		try {
			ProtoDB db = getProtoDBInstance();

			String filename = newFilename.replace("'", "''");

			try {
				if (db.getDBType() == DBType.Sqlite) lock.lock();

				String sql = String.format(
						"UPDATE Media "
						+ "SET _converterState_ID = %s "
						+ (StringUtils.isEmpty(newFilename) ? "" : String.format(", convertedFileName = '%s' ", filename))
						+ "WHERE ID = %s", 
							value,
							id);
				db.executeNonQuery(sql);
				
			}
			finally {
				if (db.getDBType() == DBType.Sqlite) lock.unlock();
				
			}

		} catch (Exception e) {
			this.getMainLog().Error(String.format("Failed to set download complete"), e);
		}						
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#saveConversion(int, int)
	 */
	@Override
	public void saveConversion(int id, int completedValue) {
		saveConversion(id, StringUtils.EMPTY, completedValue);
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#getSeriesByEpisode(int)
	 */
	@Override
	public Series getSeriesByEpisode(int id) {
		List<Series> series = null;
		try {
			ProtoDB db = getProtoDBInstance();
			
			try {
				if (db.getDBType() == DBType.Sqlite) lock.lock();

				series =
					db.search(
						SearchOptions.newBuilder(JukeboxDomain.Series.getDefaultInstance())
						.addFieldName("season.episode.ID")
						.addSearchArgument(id)
						.addOperator(ProtoDBSearchOperator.Equals));	

			}
			finally {
				if (db.getDBType() == DBType.Sqlite) lock.unlock();
			}
		}
		catch (Exception e) {
			this.getMainLog().Error("Failed to retrieve movie listing from DB", e);
		}
		
		if (series.size() > 0)
			return series.get(0);
		else
			return null;

	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.IDatabase#save(se.qxx.jukebox.domain.JukeboxDomain.Season)
	 */
	@Override
	public Season save(Season season) {
		try {
			ProtoDB db = getProtoDBInstance();

			try {
				if (db.getDBType() == DBType.Sqlite) lock.lock();
				
				return db.save(season);
			}
			finally {
				if (db.getDBType() == DBType.Sqlite) lock.unlock();
			}
		}
		catch (Exception e) {
			this.getMainLog().Error("Failed to store episode to DB", e);
			
			return null;
		}
	}

}
