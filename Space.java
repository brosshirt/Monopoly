public class Space {
	public String name;
	public char type;	//p for property, a for action(chance, gotojail,freeparking,GO,etc) rr(railroad), u(utility)
	public String color;	//b(brown),lb(light blue),p (purple),
	public int cost;
	public String owner;
	public int rent;
	public int[] rents;
	public int houses;
	public int priceHouses;
	public boolean monopoly;
	public boolean mortgage;
	//Remember to add rent values. Maybe in another method. Prob not
	// For action, you just run the method, if Board.get(location).name = CommunityChest. CommunityChest()
	Space(String name, char type){
		this.name = name;
		this.type = type;
	}
	// for properties
	Space(String name, char type, String owner, String color, int cost, int[] rents, int houses, int priceHouses, boolean monopoly){
		this.name = name;
		this.type = type;
		this.owner = owner;
		this.color = color;
		this.cost = cost;
		this.rents = rents;
		this.owner = owner;
		this.houses = houses;
		this.priceHouses = priceHouses;
		this.monopoly = monopoly;
		if (monopoly == false){
			this.rent =  rents[0];
		}
		else {
			this.rent = rents[houses+1];
		}
	}
	// for r
	Space(String name, char type, String owner, int cost){
		this.name = name;
		this.type = type;
		this.owner = owner;
		this.cost = cost;
		this.mortgage = false;
		// rent is going to be equal to 25 * 2^(n-1) where n is the number of railroads owned by the player.
	}
	// for u
	Space(String name, char type, String owner, int cost, boolean monopoly){ // rent value is simply a multiplier of diceroll
		this.name = name;
		this.type = type;
		this.owner = owner;
		this.cost = cost;
		this.monopoly = monopoly;
		this.mortgage = false;
	}



}
