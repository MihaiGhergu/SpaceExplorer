import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class that implements the channel used by headquarters and space explorers to communicate.
 */
public class CommunicationChannel {

	/**
	 * Creates a {@code CommunicationChannel} object.
	 */
	public CommunicationChannel() {
	}

	/**
	 * Puts a message on the space explorer channel (i.e., where space explorers write to and 
	 * headquarters read from).
	 * 
	 * @param message
	 *            message to be put on the channel
	 */

	/**
	 * Aici stocez mesajele, atat pentru HQ cat si pt SE
	 * */
	ArrayBlockingQueue<Message> spaceExplorerChannel = new ArrayBlockingQueue<Message>(2000);
	ArrayBlockingQueue<Message> headQuarterChannel = new ArrayBlockingQueue<Message>(2000);

	/**
	 * Aceste 2 semafoare le folosesc pentru ca imediat dupa o metoda de put,
	 * sa am o metoda de get din spaceExplorerChannel. Cand dau put, blochez
	 * primul semafor si il eliberez pe al doilea, iar cand dau get, deblochez
	 * primul semafor si il blochez pe al doilea.
	 */
	Semaphore semaphore1 = new Semaphore(1);
	Semaphore semaphore2 = new Semaphore(0);
	/**
	 * Folosesc acest lock si variabila sw care poate sa fie 0 sau 1,
	 * pentru ca atunci cand dau put in HQ, vreau sa pun pe canal
	 * mesajele in forma parinte-copil.
	 */
	int sw = 0;
	ReentrantLock lock = new ReentrantLock();

	public void putMessageSpaceExplorerChannel(Message message) {
		try {
			semaphore1.acquire();
			spaceExplorerChannel.put(message);
		} catch(InterruptedException e){}
		semaphore2.release();
	}

	/**
	 * Gets a message from the space explorer channel (i.e., where space explorers write to and
	 * headquarters read from).
	 * 
	 * @return message from the space explorer channel
	 */
	public Message getMessageSpaceExplorerChannel() {
		Message result = null;
		try {
			semaphore2.acquire();
			result = spaceExplorerChannel.take();
		} catch(InterruptedException e) {}
		semaphore1.release();
		return result;
	}

	/**
	 * Puts a message on the headquarters channel (i.e., where headquarters write to and 
	 * space explorers read from).
	 * 
	 * @param message
	 *            message to be put on the channel
	 */

	public void putMessageHeadQuarterChannel(Message message) {
		try {
			if(message.getData().equals("END"))
				headQuarterChannel.put(message);
			else{
				if(sw == 0){
					lock.lock();
					headQuarterChannel.put(message);
					sw = 1;
				} else if(sw == 1){
					headQuarterChannel.put(message);
					lock.unlock();
					sw = 0;
					}
			}
		} catch(InterruptedException e){}
	}

	/**
	 * Gets a message from the headquarters channel (i.e., where headquarters write to and
	 * space explorer read from).
	 * 
	 * @return message from the header quarter channel
	 */
	public Message getMessageHeadQuarterChannel() {
		Message result = null;
		try {
			result = headQuarterChannel.take();
		} catch(InterruptedException e){}
		return result;
	}

}
