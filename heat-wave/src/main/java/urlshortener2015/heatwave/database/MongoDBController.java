package urlshortener2015.heatwave.database;

import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.MongoClient;

import urlshortener2015.common.domain.ShortURL;

public class MongoDBController implements DatabaseController {
	
	private static MongoTemplate singleton;
	private static MongoClient mongoClient;
	
	private static final Logger log = LoggerFactory
			.getLogger(MongoDBController.class);

	@Autowired
	protected MongoTemplate tmpl;
	
	/**
     * Conecta con la base de datos si no había conexión previa
     * @return Conexión con la base de datos
     */
    public static MongoTemplate getMongoTemplate(){
        return getMongoTemplate(false);
    }
    
    /**
     * Conecta con la base de datos si no había conexión previa o reinicia la
     * conexión
     * @param forceReload true para reiniciar la conexión, false en caso contrario
     * @return Conexión con la base de datos
     */
    public static MongoTemplate getMongoTemplate(boolean forceReload){
        if (forceReload){
        	singleton = null;
        	mongoClient.close();
        	mongoClient = null;
        }
        if (singleton == null){
            Properties prop = new Properties();
            try {
                InputStream in = MongoDBController.class.getResourceAsStream("application.properties");
                prop.load(in);
                singleton = connect(prop);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
        return singleton;
    }
    
    /**
     * Establece una conexión con la base de datos Mongo a partir de un
     * fichero de configuración
     * @param prop Propiedades del fichero de configuración
     * @return Conexión con la base de datos
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     * @throws UnknownHostException
     */
   private static MongoTemplate connect(Properties prop)
       throws InstantiationException, IllegalAccessException, ClassNotFoundException, UnknownHostException {
	   String port = prop.getProperty("database.mongo.port", null);
	   port = port != null ? ":" + port : "";
	   mongoClient = new MongoClient(prop.getProperty("database.mongo.host") + port);
	   MongoTemplate tmpl = new MongoTemplate(mongoClient, prop.getProperty("database.mongo.db"));
	   return tmpl;
   }

	@Override
	public ShortURL findByKey(String id) {
		return null;
	}

	@Override
	public ShortURL save(ShortURL su) {
		return null;
	}

	@Override
	public ShortURL mark(ShortURL su, boolean safeness) {
		return null;
	}

	@Override
	public void update(ShortURL su) {
		
	}

	@Override
	public void delete(String hash) {
		
	}

	@Override
	public Long count() {
		return null;
	}

	@Override
	public List<ShortURL> list(Long limit, Long offset) {
		return null;
	}

	@Override
	public List<ShortURL> findByTarget(String target) {
		return null;
	}

}
