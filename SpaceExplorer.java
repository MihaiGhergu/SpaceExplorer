import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import java.nio.charset.StandardCharsets;
/**
 * Class for a space explorer.
 */
public class SpaceExplorer extends Thread {

	/**
	 * Creates a {@code SpaceExplorer} object.
	 * 
	 * @param hashCount
	 *            number of times that a space explorer repeats the hash operation
	 *            when decoding
	 * @param discovered
	 *            set containing the IDs of the discovered solar systems
	 * @param channel
	 *            communication channel between the space explorers and the
	 *            headquarters
	 */

	private Integer hashCount;
	private Set<Integer> discovered;
	private CommunicationChannel channel;

	public SpaceExplorer(Integer hashCount, Set<Integer> discovered, CommunicationChannel channel) {
		this.hashCount = hashCount;
		this.discovered = discovered;
		this.channel = channel;
	}
/**
 * In cadrul acestei functii, iau cate 2 mesaje sincronizate din
 * canalul HQ-urilor verific sa nu fie exit sau end, apoi construiesc
 * mesajul final decriptat, doar daca am doilea nod nu a fost vizitat,
 * si il pune in SE.
 */
	@Override
	public void run() {
		while(true){
			Message child , parent;
			synchronized (channel) {
				parent = channel.getMessageHeadQuarterChannel();
				if((parent.getData()).equals("END"))
					continue;
				if((parent.getData()).equals("EXIT"))
					return;
				child = channel.getMessageHeadQuarterChannel();
			}
				int childSS = child.getCurrentSolarSystem();
				int parrentSS = parent.getCurrentSolarSystem();
				if (!discovered.contains(childSS)) {
					String encrData = encryptMultipleTimes(child.getData(), hashCount);
					Message ans = new Message(parrentSS, childSS, encrData);
					channel.putMessageSpaceExplorerChannel(ans);
					discovered.add(childSS);
				}
		}
	}

	/**
	 * Applies a hash function to a string for a given number of times (i.e.,
	 * decodes a frequency).
	 * 
	 * @param input
	 *            string to he hashed multiple times
	 * @param count
	 *            number of times that the string is hashed
	 * @return hashed string (i.e., decoded frequency)
	 */
	private String encryptMultipleTimes(String input, Integer count) {
		String hashed = input;
		for (int i = 0; i < count; ++i) {
			hashed = encryptThisString(hashed);
		}
		return hashed;
	}

	/**
	 * Applies a hash function to a string (to be used multiple times when decoding
	 * a frequency).
	 * 
	 * @param input
	 *            string to be hashed
	 * @return hashed string
	 */
	private static String encryptThisString(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] messageDigest = md.digest(input.getBytes(StandardCharsets.UTF_8));

			// convert to string
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++) {
				String hex = Integer.toHexString(0xff & messageDigest[i]);
				if (hex.length() == 1)
					hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
}
