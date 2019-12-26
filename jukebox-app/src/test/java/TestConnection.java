import org.junit.Test;
import se.qxx.android.jukebox.comm.JukeboxConnectionHandler;

public class TestConnection {

    @Test
    public void Should_not_cast_exception_on_init() {
        JukeboxConnectionHandler jh = new JukeboxConnectionHandler("127.0.0.1", 2152);
    }
}