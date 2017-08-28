package creation;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MathsAidBashCreation implements Creation{
	public static final String BASH = "/bin/bash";
	public static final File CREATIONS_FOLDER = new File(
			System.getProperty("user.dir") + System.getProperty("file.separator") + "creations");
	public static final String MP4 = ".mp4";
	public static final String WAV = ".wav";
	
	public static final String DEFAULT_COLOUR = "blue";
	public static final String DEFAULT_FONT_COLOUR = "white";
	public static final int DEFAULT_LENGTH = 3;
	
	private final String _fullFileName;
	private String _creationName;
	private String _audioComponent; 
	private String _videoComponent; 
	private String _colour;
	private String _fontColour;
	private int _length;
	
	
	public MathsAidBashCreation(String name) {
		this(name, DEFAULT_COLOUR, DEFAULT_FONT_COLOUR, DEFAULT_LENGTH);
	}
	
	public MathsAidBashCreation(String name, String colour) {
		this(name, colour, DEFAULT_FONT_COLOUR, DEFAULT_LENGTH);
	}
	public MathsAidBashCreation(String name, String colour, String fontColour) {
		this(name, colour, fontColour, DEFAULT_LENGTH);
	}
	
	public MathsAidBashCreation(String name, String colour, String fontColour, int length) {
		if (name == null) {
			throw new CreationException("Invalid creation name (null)");
		}
		if (name == "") {
			throw new CreationException("Invalid creation name ()");
		}
		if (colour == null || colour == "") {
			colour = "blue";
		}
		if (fontColour == null || fontColour == "") {
			fontColour = "white";
		}
		_creationName = name;
		_colour = colour;
		_fontColour = fontColour;
		_length = length;
		
		_audioComponent = name + "_ac." + WAV;
		_videoComponent = name + "_vc." + MP4;

		_fullFileName = _creationName + MP4;
	}
	
	@Override
	public void create() {
		creationFolder();
		createAudioComponent();
		createVideoComponent();
		combineAudioAndVideo();
		deleteCreationComponents();
		
	}
	
	public void creationFolder() {
		if (!CREATIONS_FOLDER.exists()) {
			CREATIONS_FOLDER.mkdir();
		}
	}
	
	private void createVideoComponent() {
		String command = "ffmpeg -f lavfi -i " + "color=c="+_colour+":s=320x240:d="+_length+" -vf "
				+ "\"drawtext=fontfile=/path/to/font.ttf:fontsize=30:"
				+ " fontcolor="+_fontColour+":x=(w-text_w)/2:y=(h-text_h)/2:text='" + _creationName + "'\" "
				+ "\"" + _videoComponent + "\"";

		processCommand(command);
		
	}
	
	private void createAudioComponent() {
		String command = "ffmpeg -f alsa -i \"default\" -t "+ _length +" \"" + _audioComponent + "\"";

		processCommand(command);
	}
	
	private void combineAudioAndVideo() {
		String command = "ffmpeg -i " + _videoComponent +" -i " + _audioComponent + " -c:v copy -c:a aac"
				+ " -strict experimental " + _creationName + MP4;

		processCommand(command);
	}
	
	private void deleteCreationComponents() {
		deleteFile(_videoComponent);
		deleteFile(_audioComponent);
	}
	
	private void deleteFile(String filename) {
		Path path = Paths.get(CREATIONS_FOLDER + System.getProperty("file.separator") + filename);
		try {
			Files.delete(path);
		} catch (NoSuchFileException x) {
			System.err.format("%s: no such" + " file or directory%n", path);
		} catch (DirectoryNotEmptyException x) {
			System.err.format("%s not empty%n", path);
		} catch (IOException x) {
			// File permission problems are caught here.
			System.err.println(x);
		}
	}
	
	public void play() {
		String command = "ffplay -autoexit \""+_fullFileName+"\"";
		
		processCommand(command);
	}
	
	public void delete() {
		deleteFile(_fullFileName);
	}
	
	public String name() {
		return _creationName;
	}
	
	public File file() {
		return new File(CREATIONS_FOLDER + System.getProperty("file.separator") +_fullFileName);
	}
	
	protected final void processCommand(String command) {
		ProcessBuilder pb = new ProcessBuilder(BASH, "-c", command);
		pb.directory(CREATIONS_FOLDER);

		try {
			java.lang.Process process = pb.start();
			process.waitFor();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}


	
}


