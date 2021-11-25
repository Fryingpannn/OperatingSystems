/**
 * Class Monitor
 * To synchronize dining philosophers.
 *
 * @author Serguei A. Mokhov, mokhov@cs.concordia.ca
 */
import java.util.*;
public class Monitor
{
	/*
	 * ------------
	 * Data members
	 * ------------
	 */
	
	//all possible states of a philosopher
	enum PhilosopherStates {TALKING, THINKING, HUNGRY, EATING};
	
	int numOfPhilosophers;
	//state of each philosopher
	PhilosopherStates[] stateOfPhilosophers;
	
	//queues to prevent starvation
	Queue<Integer> WaitToEat = new ArrayDeque<> ();
	Queue<Integer> WaitToTalk = new ArrayDeque<> ();
	
	//keeps track of whether anyone is talking
	boolean anyoneTalking = false;


	/**
	 * Constructor
	 */
	public Monitor(int piNumberOfPhilosophers)
	{
		numOfPhilosophers = piNumberOfPhilosophers;
		//initializing the state of philosophers
		stateOfPhilosophers = new PhilosopherStates[numOfPhilosophers];
		for(int i = 0; i < numOfPhilosophers; i++) {
			stateOfPhilosophers[i] = PhilosopherStates.THINKING;
		}
	}

	/*
	 * -------------------------------
	 * User-defined monitor procedures
	 * -------------------------------
	 */

	/**
	 * Grants request (returns) to eat when both chopsticks/forks are available.
	 * Else forces the philosopher to wait()
	 */
	public synchronized void pickUp(final int piTID)
	{
		// the current philosopher's index
		int CurrentPhilosopher = piTID - 1;
		
		//the left and right philosophers' index
		int RightPhilosopher = CurrentPhilosopher + 1;
		int LeftPhilosopher = CurrentPhilosopher - 1;
		
		//update the state of the philosopher
		stateOfPhilosophers[CurrentPhilosopher] = PhilosopherStates.HUNGRY;

		// adding the philosopher to the queue
		WaitToEat.add(piTID);
		
		// since the philosophers are sitting in a circle, we go back to the beginning of the list once we reach the end
		if (CurrentPhilosopher == 0)
		{
			LeftPhilosopher = numOfPhilosophers - 1;
		}
		else if (CurrentPhilosopher == numOfPhilosophers-1)
		{
			RightPhilosopher = 0;
		}
		
		//busy wait until it is the philosopher's turn to eat
		while (true) {
			
			// allow the philosopher to eat only when the left and right philosophers aren't eating
			// make sure the philosopher is not sitting on the left or right of first philosopher in the queue
			if(stateOfPhilosophers[RightPhilosopher] != PhilosopherStates.EATING && stateOfPhilosophers[LeftPhilosopher] != PhilosopherStates.EATING && (WaitToEat.peek() == piTID ||(WaitToEat.peek()!= LeftPhilosopher+1 && WaitToEat.peek()!=RightPhilosopher+1)))
			{
				stateOfPhilosophers[CurrentPhilosopher] = PhilosopherStates.EATING;
				// remove the philosopher from the queue
				WaitToEat.remove(piTID);
				return;
			}
			else 
			{
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
	}

	/**
	 * When a given philosopher's done eating, they put the chopstiks/forks down
	 * and let others know they are available.
	 */
	public synchronized void putDown(final int piTID)
	{
		// reset the philosopher's state to thinking
		stateOfPhilosophers[piTID-1] = PhilosopherStates.THINKING;
		// notify all other philosophers that she is done eating
		notifyAll();
	}

	/**
	 * Only one philosopher at a time is allowed to philosophy
	 * (while she is not eating).
	 */
	//added the philosopher's ID as a parameter so that we can add her to the queue.
	public synchronized void requestTalk(final int piTID)
	{
		// adding the philosopher to the queue
		WaitToTalk.add(piTID);
		while (true) {

			// if no one is talking and the philosopher is not eating, they can talk
			if(stateOfPhilosophers[piTID-1] != PhilosopherStates.EATING && WaitToTalk.peek() == piTID && !anyoneTalking)
			{
				anyoneTalking = true;
				//remove the philosopher from the queue
				WaitToTalk.remove();
				return;
			}
			else 
			{
				try {
					wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * When one philosopher is done talking stuff, others
	 * can feel free to start talking.
	 */
	public synchronized void endTalk()
	{
		anyoneTalking = false;
		// notify all other philosophers that she is done talking
		notifyAll();
	}
}

// EOF
