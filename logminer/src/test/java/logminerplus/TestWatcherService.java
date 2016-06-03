package logminerplus;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

public class TestWatcherService {
	
	private WatchService watcher;
	
	public TestWatcherService(Path path) throws IOException {
		watcher = FileSystems.getDefault().newWatchService();
		path.register(watcher, 
				StandardWatchEventKinds.ENTRY_CREATE, 
				StandardWatchEventKinds.ENTRY_DELETE, 
				StandardWatchEventKinds.ENTRY_MODIFY);
	}
	
	public void handleEvents() throws InterruptedException{
		while(true){
			WatchKey key = watcher.take();
			for(WatchEvent<?> event : key.pollEvents()){
				WatchEvent.Kind kind = event.kind();
				if(kind == StandardWatchEventKinds.OVERFLOW){
                    continue;  
                }
				WatchEvent<Path> e = (WatchEvent<Path>)event;
                Path fileName = e.context();
                System.out.printf("Event %s has happened,which fileName is %s%n",kind.name(),fileName);
                if(kind == StandardWatchEventKinds.ENTRY_CREATE) {
                	File f = new File("D://test//"+fileName);
                    if(f.isFile()){
                    	System.out.println("是文件");
                    } else {
                    	System.out.println("不是文件");
                    }
                }
			}
			if(!key.reset()){
				break;
			}
		}
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
//		URI uri = URI.create("D:\\test");
		Path path=Paths.get("D:\\test");
		TestWatcherService test = new TestWatcherService(path);
		test.handleEvents();
	}
	

}
