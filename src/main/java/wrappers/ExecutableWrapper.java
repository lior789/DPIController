package wrappers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ProcessBuilder.Redirect;
import java.util.List;

import org.apache.log4j.Logger;

import Controller.DPIController;

/**
 * this class wrap an executable from the application resources
 * 
 * @author ubuntu
 * 
 */
public class ExecutableWrapper {

	private final File _executableFile;
	private static final Logger LOGGER = Logger
			.getLogger(ExecutableWrapper.class);

	public ExecutableWrapper(String exeResource, String filename)
			throws FileNotFoundException, IOException {
		_executableFile = createExecutable(exeResource, filename);
	}

	private Process proc;

	public void runProcess(List<String> args) throws IOException {
		stopProcess();
		String exeCommand = String
				.format("sudo ./%s %s", _executableFile, args);
		LOGGER.info("executing " + exeCommand);
		args.add(0, "./" + _executableFile.toString());
		args.add(0, "sudo");

		ProcessBuilder pb = new ProcessBuilder(args);
		pb.redirectOutput(Redirect.INHERIT);
		pb.redirectError(Redirect.INHERIT);
		proc = pb.start();
	}

	private File createExecutable(String exeResource, String filename)
			throws FileNotFoundException, IOException {
		File exeFile = new File(filename);
		if (exeFile.exists())
			return exeFile;
		InputStream exeStream = DPIServiceWrapper.class
				.getResourceAsStream(exeResource);

		FileOutputStream out = new FileOutputStream(exeFile);
		byte[] temp = new byte[32768];
		int rc;
		while ((rc = exeStream.read(temp)) > 0) {
			out.write(temp, 0, rc);
		}
		exeStream.close();
		out.close();
		LOGGER.trace(exeResource + " executable been created");
		exeFile.setExecutable(true);
		return exeFile;
	}

	public void stopProcess() {
		if (proc != null) {
			proc.destroy();
		}

	}
}
