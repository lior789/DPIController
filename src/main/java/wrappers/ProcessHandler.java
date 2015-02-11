package wrappers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ProcessBuilder.Redirect;
import java.util.List;

public class ProcessHandler {

	private final File _executableFile;

	public ProcessHandler(String exeResource) throws FileNotFoundException,
			IOException {
		_executableFile = createExecutable(exeResource);
	}

	private Process proc;

	public void runProcess(List<String> args) throws IOException {
		stopProcess();
		String exeCommand = String
				.format("sudo ./%s %s", _executableFile, args);
		System.out.println(exeCommand);
		args.add(0, "./" + _executableFile.toString());
		args.add(0, "sudo");

		ProcessBuilder pb = new ProcessBuilder(args);
		pb.redirectOutput(Redirect.INHERIT);
		pb.redirectError(Redirect.INHERIT);
		proc = pb.start();

	}

	private File createExecutable(String exeResource)
			throws FileNotFoundException, IOException {
		InputStream exeStream = DPIServiceWrapper.class
				.getResourceAsStream(exeResource);
		System.out.println(exeStream != null);
		File exeFile = new File("process.exe");
		exeFile.setExecutable(true);
		FileOutputStream out = new FileOutputStream(exeFile);
		byte[] temp = new byte[32768];
		int rc;
		while ((rc = exeStream.read(temp)) > 0) {
			out.write(temp, 0, rc);
		}
		exeStream.close();
		out.close();
		System.out.println(exeResource + " executable been created");
		return exeFile;
	}

	public void stopProcess() {
		if (proc != null) {
			proc.destroy();
		}

	}
}
