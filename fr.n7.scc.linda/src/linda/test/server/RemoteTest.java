package linda.test.server;

import linda.Linda;

public class RemoteTest {
	
	protected final Linda linda;

	public RemoteTest(String name) {
		// TODO Auto-generated constructor stub
		this.linda = new linda.server.LindaClient(name);
	}

}
