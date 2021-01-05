public class Player {
	public String name;
	public int balance;
	public int location;
	public boolean getOutOfJail;
	public boolean jail;
	public boolean turnOver;
	public boolean eliminate;
	public String eliminator;

	Player(String name){
		this.name = name;
		this.balance = 1500;
		this.location = 0;
		this.getOutOfJail = false;
		this.jail = false;
		this.turnOver = false;
		this.eliminate = false;
		this.eliminator = " ";
	}

	public int getBalance() {
		return balance;
	}

	public int getLocation() {
		return location;
	}

}

