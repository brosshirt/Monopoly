import java.util.ArrayList;
import java.util.*;
import java.util.Scanner;

public class MonopolyGame {

	public static void main(String[]args){
		Scanner scan = new Scanner(System.in);
		ArrayList <Player> players = new ArrayList();
		ArrayList <Space> board = new ArrayList();
		initializeGame(players,board);
		int i = 0;
		while (noWinner(players)){
			int roll = 0;
			players.get(i).turnOver = false;
			System.out.println();
			System.out.println(players.get(i).name + "'s turn!");
			System.out.println();
			System.out.println("balance: " + players.get(i).balance);
			System.out.print("location: " + board.get(players.get(i).location).name);
			if (board.get(players.get(i).location).type == 'p') {
				System.out.println(" (" + board.get(players.get(i).location).color + ")");
			}
			else {
				System.out.println();
			}
			String input = "g";
			
			if (!players.get(i).jail) {
				beforeRoll(players.get(i), players, board);
			}
			if (players.get(i).jail) {
				jail(players.get(i), players, board);
				if (players.get(i).jail) {
					int die1 = (int)((Math.random()*6)+1);
					int die2 = (int)((Math.random()*6)+1);
					roll = die1 + die2;
					if (die1 == die2) {
						System.out.println("You rolled doubles! Two " + die1 + "'s");
						players.get(i).jail = false;
					}
					else {
						System.out.println(die1 + " and " + die2 + ", still in jail");
						i = (i+1) % players.size();
						continue;
					}
				}
			}
			int preRollLocation = players.get(i).location;
			boolean doubles = false;
			if (roll == 0) { // meaning that the person didn't already roll from jail
				int die1 = (int)((Math.random()*6)+1);
				int die2 = (int)((Math.random()*6)+1);
				roll = die1 + die2;
				System.out.println("You rolled a " + die1 + " and " + die2 + " for " + roll);
				if (die1 == die2) {
					doubles = true;	
				}
			}
			players.get(i).location = (players.get(i).location + roll) % 40;
			Go(preRollLocation, players.get(i));
			if (board.get(players.get(i).location).type == 'p') {
				System.out.println("You have landed on " + board.get(players.get(i).location).name + " (" + board.get(players.get(i).location).color + ")");
				System.out.println();
			}
			else {
				System.out.println("You have landed on " + board.get(players.get(i).location).name);
				System.out.println();
			}
			 
			if (board.get(players.get(i).location).type == 'a') {
				action(players.get(i), players, board);													
			}
			if (board.get(players.get(i).location).type == 'r' && players.get(i).turnOver == false) {
				railroad(players.get(i), players, board.get(players.get(i).location), board, false);
			}
			if (board.get(players.get(i).location).type == 'u' && players.get(i).turnOver == false) {
				utility(players.get(i), players, board.get(players.get(i).location), board, roll, false);
			}
			if (board.get(players.get(i).location).type == 'p' && players.get(i).turnOver == false) {
				property(players.get(i), players, board.get(players.get(i).location), board);
			}
			// Problems with getting the turn to move to the next player.
			// I'd generally want to increment i by one and % by size. But the problem is if i is 2, and player 2 gets eliminated. Then the new player 2 should get the move, but if i increments by 1 then a player gets skipped. 
			monopoly(players, board);
			boolean current = players.get(i).eliminate;
			boolean eliminated = eliminate(players, i, board);
			if (!current && doubles && !players.get(i).jail) {
				continue;
			}
			while (!eliminated && !players.get(i).jail) {
				System.out.println();
				System.out.println("Press i to receive info on properties, location, and balance");
				System.out.println("Press b to build");
				System.out.println("Press t to trade, mortgage properties, or sell houses");
				System.out.println("Press u to unmortgage");
				System.out.println("Or press e to end turn");
				
				input = scan.next();
				if (input.equals("u")) {
					unmortgage(players.get(i), board);
				}
				if (input.equals("i")) {
					printInfo(players.get(i),board);
				}
				if (input.equals("b")) {
					build(players.get(i),board); // Grab all the eligible properties (properties part of monopolies owned by the player), print them with a number next to them and if the user inputs the number it adds a house to that property
				}
				if (input.equals("e")) {
					 break;
				}
				if (input.equals("t")) {
					raiseMoney(players.get(i), players, board);
					monopoly(players,board);
				}
				else {
					continue;
				}
			}
			if (!eliminated) { 
				i = (i+1) % players.size();
			}
			else if (i == players.size()) {
				i = 0;
			}
			
		}

	}
	
	public static void unmortgage(Player p, ArrayList <Space> board) {
		Scanner scan = new Scanner (System.in);
		ArrayList <Space> mortgaged = new ArrayList();
		for (Space s : board) {
			if (s.type != 'a' && s.owner.equals(p.name)) {
				if (s.type == 'p' && s.rent == 0 || s.mortgage) {
					mortgaged.add(s);
				}
			}
		}
		if (mortgaged.size() == 0) {
			System.out.println("You don't have any properties you can unmortgage");
			return;
		}
		for (int i = 1; i<mortgaged.size()+1; i++) {
			System.out.println(i + ". " + mortgaged.get(i-1).name + ". Price to unmortgage: " + (int)(mortgaged.get(i-1).cost * .55));
		}
		System.out.println("Select the number for the property that you'd like to unmortgage");
		int input = 0;
		while (true) {
			if (!scan.hasNextInt()) {
				System.out.println("Enter an integer");
				continue;
			}
			input = scan.nextInt();
			if (input < 1 || input >= mortgaged.size()+1) {
				System.out.println("Input out of bounds");
				continue;
			}
			break;
		}
		if (p.balance < mortgaged.get(input-1).cost * .55) {
			System.out.println("You don't have enough money");
			return;
		}
		else {
			System.out.println(mortgaged.get(input-1).name + " is now unmortgaged");
			if (mortgaged.get(input-1).type == 'p') {
				updateRent(mortgaged.get(input-1));
			}
			else {
				mortgaged.get(input-1).mortgage = false;
			}
		}
	}

	public static boolean eliminate(ArrayList <Player> players, int i, ArrayList <Space> board) {
		boolean eliminate = false;
		for (int j = 0; j<players.size(); j++) {
			if (players.get(j).eliminate) {
				System.out.println(players.get(j).name + " has been removed from the game");
				transferAssets(players.get(j), players, board);
				players.remove(j);
				if (j <= i) {
					eliminate = true;
				}
			}
		}
		return eliminate;
	}
	
	public static void transferAssets(Player p, ArrayList <Player> players, ArrayList <Space> board) {
		for (Space s : board) {
			if (s.type != 'a' && s.owner.equals(p.name)) {
				s.owner = p.eliminator;
			}
		}
		for (Player player : players) {
			if (player.name.contentEquals(p.eliminator)) {
				player.balance += p.balance;
			}
		}
	}
	
	public static boolean noWinner(ArrayList <Player> players){
		if (players.size()>1){
			return true;
		}
		System.out.println(players.get(0).name + " is the winner!");
		return false;
	}

	public static void initializeGame(ArrayList <Player> players, ArrayList <Space> board){
		Scanner scan = new Scanner(System.in);
		int numPlayers = 0;
		while (numPlayers < 2 || numPlayers > 4){
			System.out.println("Enter players 2-4");
			if (scan.hasNextInt()) {
				numPlayers = scan.nextInt();
			}
			else {
				scan.next();
				continue;
			}
		}
		for (int i = 0; i<numPlayers; i++){
			System.out.println("Player " + (i+1) + " , enter your name");
			Player p = new Player(scan.next());
			players.add(p);
		}
		int [] Mediterranean = {2, 4, 10, 30, 90, 160, 250};
		int [] Baltic = {4, 8, 20, 60, 180, 320, 450};
		int [] Oriental = {6, 12, 30,90,270,400,550};
		int [] Vermont = {6, 12, 30, 90, 270, 400, 550};
		int [] Connecticut = {8, 16, 40, 100, 300, 450, 600};
		int [] StCharles = {10, 20, 50, 150, 450, 625, 750};
		int [] States = {10, 20, 50, 150, 450, 625, 750};
		int [] Virginia = {12, 24, 60, 180, 500, 700, 900};
		int [] StJames = {14, 28, 70, 200, 550, 750, 950};
		int [] Tennessee = {14, 28, 70, 200, 550, 750, 950};
		int [] NewYork = {16, 32, 80, 220, 600, 800, 1000};
		int [] Kentucky = {18, 36, 90, 250, 700, 875, 1050};
		int [] Indiana = {18, 36, 90, 250, 700, 875, 1050};
		int [] Illinois = {20, 40, 100, 300, 750, 925, 1100};
		int [] Atlantic = {22, 44, 110, 330, 800, 975, 1150};
		int [] Ventnor = {22, 44, 110, 330, 800, 975, 1150};
		int [] Marvin = {24, 48, 120, 360, 850, 1025, 1200};
		int [] Pacific = {26, 52, 130, 390, 900, 1100, 1275};
		int [] NorthCarolina = {26, 52, 130, 390, 900, 1100, 1275};
		int [] Pennsylvania = {28, 56, 150, 450, 1000, 1200, 1400};
		int [] ParkPlace = {35, 70, 175, 500, 1100, 1300, 1500};
		int [] BoardWalk = {50, 100, 200, 600, 1400, 1700, 2000};
		
		
		board.add(new Space("GO", 'a'));
		board.add(new Space("Mediterranean Avenue", 'p', " ", "brown", 60, Mediterranean, 3, 50, true));
		board.add(new Space("Community Chest", 'a'));
		board.add(new Space("Baltic Avenue", 'p', " ", "brown", 60, Baltic, 3, 50, true));
		board.add(new Space("Income Tax", 'a'));
		board.add(new Space("Reading Railroad", 'r', " ", 200)); // rent is going to be equal to 25 * 2^(n-1) where n is the number of railroads owned by the player.
		board.add(new Space("Oriental Avenue", 'p', " ", "light blue", 100, Oriental, 0, 50, false));
		board.add(new Space("Chance", 'a'));
		board.add(new Space("Vermont Avenue", 'p', " ", "light blue", 100, Vermont, 0, 50, false));
		board.add(new Space("Connecticut Avenue", 'p', " ", "light blue", 120, Connecticut, 0, 50, false));
		board.add(new Space("Jail", 'a'));
		board.add(new Space("St. Charles Place", 'p', " ", "pink", 140, StCharles, 0, 100, false));
		board.add(new Space("Electric Company", 'u', " ", 150, false));
		board.add(new Space("States Avenue", 'p', " ", "pink", 140, States, 0, 100, false));
		board.add(new Space("Virginia Avenue", 'p', " ", "pink", 160, Virginia, 0, 100, false));
		board.add(new Space("Pennsylvania Railroad", 'r', " ", 200));
		board.add(new Space("St. James Place", 'p', " ", "orange", 180, StJames, 0, 100, false));
		board.add(new Space("Community Chest", 'a'));
		board.add(new Space("Tennessee Avenue", 'p', " ", "orange", 180, Tennessee, 0, 100, false));
		board.add(new Space("New York Avenue", 'p', " ", "orange", 200, NewYork, 0, 100, false));
		board.add(new Space("Free Parking", 'a'));
		board.add(new Space("Kentucky Avenue", 'p', " ", "red", 220, Kentucky, 0, 150, false));
		board.add(new Space("Chance", 'a'));
		board.add(new Space("Indiana Avenue", 'p', " ", "red", 220, Indiana, 0, 150, false));
		board.add(new Space("Illinois Avenue", 'p', " ", "red", 240, Illinois, 0, 150, false));
		board.add(new Space("B & O Railroad", 'r', " ", 200));
		board.add(new Space("Atlantic Avenue", 'p', " ", "yellow", 260, Atlantic, 0, 150, false));
		board.add(new Space("Ventnor Avenue", 'p', " ", "yellow", 260, Ventnor, 0, 150, false));
		board.add(new Space("Water Works", 'u', " ", 150, false));
		board.add(new Space("Marvin Gardens", 'p', " ", "yellow", 280, Marvin, 0, 150, false));
		board.add(new Space("GO TO JAIL", 'a'));
		board.add(new Space("Pacific Avenue", 'p', " ", "green", 300, Pacific, 0, 200, false));
		board.add(new Space("North Carolina Avenue", 'p', " ", "green", 300, NorthCarolina, 0, 200, false));
		board.add(new Space("Community Chest", 'a'));
		board.add(new Space("Pennsylvania Avenue", 'p', " ", "green", 320, Pennsylvania, 0, 200, false));
		board.add(new Space("Short Line", 'r', " ", 200));
		board.add(new Space("Chance", 'a'));
		board.add(new Space("Park Place", 'p', " ", "blue", 350, ParkPlace, 0, 200, false));
		board.add(new Space("Luxury Tax", 'a'));
		board.add(new Space("BoardWalk", 'p', " ", "blue", 400, BoardWalk, 0, 200, false));
		
		


	}

	public static void printInfo(Player p, ArrayList <Space> board) {
		System.out.println("balance: " + p.balance);
		System.out.print("location: " + board.get(p.location).name);
		if (board.get(p.location).type == 'p') {
			System.out.println(" (" + board.get(p.location).color + ")");
		}
		else {
			System.out.println();
		}
		System.out.println();
		ArrayList <Space> properties = new ArrayList();
		for (int i = 0; i<board.size(); i++) {
			if (board.get(i).type != 'a') {
				if (board.get(i).owner.equals(p.name)){
					properties.add(board.get(i));
				}
			}
		}
		System.out.println("Properties:");
		System.out.println();
		for (int i = 0; i<properties.size(); i++) {
			if (properties.get(i).type != 'p') {
				System.out.print(properties.get(i).name);
				if (properties.get(i).mortgage) {
					System.out.println("(mortgaged)");
				}
				else {
					System.out.println();
				}
			}
			if (properties.get(i).type == 'p') {
				System.out.print(properties.get(i).name);
				System.out.print(" (" + properties.get(i).color + ")");
				if (properties.get(i).rent == 0) {
					System.out.print("(mortgaged)");
				}
				System.out.print("; cost: " + properties.get(i).cost);
				if (properties.get(i).houses == 5) {
					System.out.println("; hotels: 1");
				}
				else {
					System.out.println("; houses: " + properties.get(i).houses);
				}
			}
		}
		int blue = 0;
		int green = 0;
		int yellow = 0;
		int lightBlue = 0;
		int brown = 0;
		int pink = 0; 
		int orange = 0;
		int red = 0; 
		int railroad = 0;
		int utilities = 0;
		System.out.println();
		System.out.println("Monopolies:");
		System.out.println();
		for (Space property : properties) {
			if (property.type == 'r') {
				railroad++;
			}
			else if (property.type == 'u') {
				utilities++;
			}
			else if (property.color.equals("blue")) {
				blue++;
			}
			else if (property.color.equals("green")) {
				green++;
			}
			else if (property.color.equals("yellow")) {
				yellow++;
			}
			else if (property.color.equals("light blue")) {
				lightBlue++;
			}
			else if (property.color.equals("brown")) {
				brown++;
			}
			else if (property.color.equals("pink")) {
				pink++;
			}
			else if (property.color.equals("orange")) {
				orange++;
			}
			else if (property.color.equals("red")) {
				red++;
			}
		}
		if (blue == 2) {
			System.out.println("Blue");
		}
		if (brown == 2) {
			System.out.println("Brown");
		}
		if (lightBlue == 3) {
			System.out.println("Light Blue");
		}
		if (pink == 3) {
			System.out.println("Pink");
		}
		if (orange == 3) {
			System.out.println("Orange");
		}
		if (red == 3) {
			System.out.println("Red");
		}
		if (yellow == 3) {
			System.out.println("Yellow");
		}
		if (green == 3) {
			System.out.println("Green");
		}
		if (railroad == 4) {
			System.out.println("Railroad");
		}
		if (utilities == 2) {
			System.out.println("Utilities");
		}
		System.out.println();
	}

	public static int roll() {
		int die1 = (int)(Math.random()*6+1);
		int die2 = (int)(Math.random()*6+1);
		int total = die1 + die2;
		System.out.println("You rolled a " + total);
		return total;
	}

	public static void action(Player p, ArrayList <Player> players, ArrayList <Space> board) {
		if (board.get(p.location).name.equals("Community Chest")) {
			communityChest(p, players, board);
		}
		if (board.get(p.location).name.equals("Chance")) {
			chance(p, players, board);
		}
		if (board.get(p.location).name.equals("GO TO JAIL")) {
			goToJail(p);
		}
		if (board.get(p.location).name.equals("Luxury Tax")) {
			luxuryTax(p, players, board);
		}
		if (board.get(p.location).name.equals("Income Tax")) {
			incomeTax(p, players, board);
		}
	}
	
	public static void Go(int oldLocation, Player p) {

		if (oldLocation>p.location) { 
			p.balance += 200;
			System.out.println("Pass Go +200");
		}
	}

	public static void communityChest(Player p, ArrayList <Player> players, ArrayList <Space> board) {
		int rand = (int)(Math.random()*16+1);
		switch (rand) {
			case 1: 
				System.out.println("Get out of jail free");
				p.getOutOfJail = true; 
				break;
			case 2:
				System.out.println("Receive $25 for services");
				p.balance += 25;
				break;
			case 3:
				System.out.println("Grand Opera Opening: Collect 50 dollars from every player");
				for (Player player : players) {
					if (!player.name.contentEquals(p.name) && player.balance < 50) {
						System.out.println(player.name + ", YOU NEED TO RAISE AT LEAST " + (50 - p.balance) + " BY MORTGAGING PROPERTIES, SELLING HOUSES, OR TRADING OR YOU WILL BE ELIMINATED");
						raiseMoney(player, players, board); // this method is going to allow you to trade or mortgage/sell your properties to get out of debt. 
						if (player.balance < 50) {
							player.eliminate = true;
							player.eliminator = " ";
							continue;
						}
					}
					player.balance -= 50;
					p.balance += 50;
				}
				
				
				break;
			case 4:
				System.out.println("Bank error in your favor, collect $200");
				p.balance += 200;
				break;
			case 5:
				System.out.println("Xmas fund matures, collect $100");
				p.balance += 100;
				break;
			case 6:
				System.out.println("Doctors fee, pay $50");
				if (p.balance < 50) {
					System.out.println("YOU NEED TO RAISE AT LEAST " + (50 - p.balance) + " BY MORTGAGING PROPERTIES, SELLING HOUSES, OR TRADING OR YOU WILL BE ELIMINATED");
					raiseMoney(p, players, board); // this method is going to allow you to trade or mortgage/sell your properties to get out of debt. 
					if (p.balance < 50) {
						p.eliminate = true;
						p.eliminator = " ";
						return;
					}
				}
				p.balance -= 50;
				break;
			case 7:
				System.out.println("You are assessed for street repairs, $40 per house, $115 per hotel.");
				int payment = 0;
				for (int i = 0; i<board.size(); i++) {
					if (board.get(i).type == 'p' && board.get(i).owner.equals(p.name)){
						if (board.get(i).houses == 5) {
							payment += 115;
						}
						else {
							payment += board.get(i).houses * 40;
						}
					}
				}
				if (p.balance < payment) {
					System.out.println("YOU NEED TO RAISE AT LEAST " + (payment - p.balance) + " BY MORTGAGING PROPERTIES, SELLING HOUSES, OR TRADING OR YOU WILL BE ELIMINATED");
					raiseMoney(p, players, board); // this method is going to allow you to trade or mortgage/sell your properties to get out of debt. 
					if (p.balance < payment) {
						p.eliminate = true;
						p.eliminator = " ";
						return;
					}
				}
				System.out.println("You have been charged $" + payment);
				p.balance -= payment;
				break;
			case 8:
				System.out.println("Sale of stock, you get $45");
				p.balance += 45;
				break;
			case 9: 
				System.out.println("Advance to GO, receive $200");
				p.balance += 200;
				p.location = 0;
				break;
			case 10:
				System.out.println("You inherit $100");
				p.balance += 100;
				break;
			case 11:
				System.out.println("Go to jail, do not pass go, do not collect $200");
				p.location = 10;
				p.jail = true; // If jail is true, run a jail method, can pay $50, can use get out of jail if get out of jail is true, or can roll (different roll method), if the roll is doubles then roll becomes roll, if roll is not doubles skip turn
				break;
			case 12:
				System.out.println("Life insurance matures, collect $100");
				p.balance += 100; 
				break;
			case 13:
				System.out.println("You have won second prize in a beauty contest, collect $10");
				p.balance += 10;
				break;
			case 14:
				System.out.println("Pay school tax of $150");
				if (p.balance < 150) {
					System.out.println("YOU NEED TO RAISE AT LEAST " + (150 - p.balance) + " BY MORTGAGING PROPERTIES, SELLING HOUSES, OR TRADING OR YOU WILL BE ELIMINATED");
					raiseMoney(p, players, board); // this method is going to allow you to trade or mortgage/sell your properties to get out of debt. 
					if (p.balance < 150) {
						p.eliminate = true;
						p.eliminator = " ";
						return;
					}
				}
				p.balance -= 150;
				break;
			case 15: 
				System.out.println("Income tax refund, collect $20");
				p.balance += 20;
				break;
			case 16:
				System.out.println("Pay hospital $100");
				if (p.balance < 100) {
					System.out.println("YOU NEED TO RAISE AT LEAST " + (100 - p.balance) + " BY MORTGAGING PROPERTIES, SELLING HOUSES, OR TRADING OR YOU WILL BE ELIMINATED");
					raiseMoney(p, players, board); // this method is going to allow you to trade or mortgage/sell your properties to get out of debt. 
					if (p.balance < 100) {
						p.eliminate = true;
						p.eliminator = " ";
						return;
					}
				}
				p.balance -= 100;
				break;
		}
	}
	
	public static void chance(Player p, ArrayList <Player> players, ArrayList <Space> board) {
		Scanner scan = new Scanner(System.in);
		int rand = (int)(Math.random()*16+1);
		switch (rand) {
			case 1: 
				System.out.println("Go back 3 spaces");
				p.location -= 3;
				if (board.get(p.location).type == 'p') {
					System.out.println("You have landed on " + board.get(p.location).name + " (" + board.get(p.location).color + ")");
				}
				else {
					System.out.println("You have landed on " + board.get(p.location).name);
				}
				break;
			case 2:
				System.out.println("Advance to St. Charles Place, if you pass GO collect $200");
				if (p.location > 11) {
					p.balance += 200;
				}
				p.location = 11;
				break;
			case 3:
				System.out.println("Take a ride to Reading Railroad, if you pass GO collect $200");
				p.location = 5;
				p.balance += 200; 
				break;
			case 4:
				System.out.println("Advance to nearest utility; if unowned, you may buy it from the bank, if owned, roll and pay the owner 10 times the amount rolled");
				if (p.location == 22) {
					System.out.println("You have landed on Water Works");
					p.location = 28;
				}
				else {
					System.out.println("You have landed on Electric Company");
					p.location = 12;
				}
				int roll = (int)(Math.random()*6 + 1) +(int)(Math.random()*6 + 1);
				utility(p, players, board.get(p.location), board, roll, true);
				p.turnOver = true;
				break;
			case 5:
				System.out.println("Advance to BoardWalk");
				p.location = 39; 
				break;
			case 6:
				System.out.println("General repairs, $25 for each house, $100 for each hotel");
				int payment = 0;
				for (int i = 0; i<board.size(); i++) {
					if (board.get(i).type == 'p' && board.get(i).owner.equals(p.name)){
						if (board.get(i).houses == 5) {
							payment += 100;
						}
						else {
							payment += board.get(i).houses * 25;
						}
					}
				}
				if (p.balance < payment) {
					System.out.println("YOU NEED TO RAISE AT LEAST " + (payment - p.balance) + " BY MORTGAGING PROPERTIES, SELLING HOUSES, OR TRADING OR YOU WILL BE ELIMINATED");
					raiseMoney(p, players, board); // this method is going to allow you to trade or mortgage/sell your properties to get out of debt. 
					if (p.balance < payment) {
						p.eliminate = true;
						p.eliminator = " ";
						return;
					}
				}
				System.out.println("You have been charged $" + payment);
				p.balance -= payment;
				break;
			case 7:
				System.out.println("Bank pays you a dividend of $50");
				p.balance += 50;
				break;
			case 8:
				System.out.println("Advance to nearest railroad, if unowned you may buy it, if owned pay owner twice the rent");
				if (p.location == 36) {
					System.out.println("Pass Go, receive $200");
					System.out.println("Advance to Reading Railroad");
					p.location = 5;
					p.balance += 200;
				}
				else if (p.location == 7) {
					System.out.println("Advance to Pennsylvania Railroad");
					p.location = 15; 
				}
				else if (p.location == 22) {
					System.out.println("Advance to B & O Railroad");
					p.location = 25;
				}
				railroad(p,players,board.get(p.location),board,true);
				p.turnOver = true;
				break;
			case 9:
				System.out.println("Advance to nearest railroad, if unowned you may buy it, if owned pay owner twice the rent");
				if (p.location == 36) {
					System.out.println("Pass Go, receive $200");
					System.out.println("Advance to Reading Railroad");
					p.location = 5;
					p.balance += 200;
				}
				else if (p.location == 7) {
					System.out.println("Advance to Pennsylvania Railroad");
					p.location = 15; 
				}
				else if (p.location == 22) {
					System.out.println("Advance to B & O Railroad");
					p.location = 25;
				}
				railroad(p,players,board.get(p.location),board,true);
				p.turnOver = true;
				break;
			case 10:
				System.out.println("Get out of jail free card");
				p.getOutOfJail = true;
				break;
			case 11:
				System.out.println("Go to jail");
				p.location = 10;
				p.jail = true;
				break;
			case 12:
				System.out.println("Pay poor tax of $15");
				if (p.balance < 15) {
					System.out.println("YOU NEED TO RAISE AT LEAST " + (15 - p.balance) + " BY MORTGAGING PROPERTIES, SELLING HOUSES, OR TRADING OR YOU WILL BE ELIMINATED");
					raiseMoney(p, players, board); // this method is going to allow you to trade or mortgage/sell your properties to get out of debt. 
					if (p.balance < 15) {
						p.eliminate = true;
						p.eliminator = " ";
						return;
					}
				}
				p.balance -= 15;
				break;
			case 13:
				System.out.println("Advance to GO, collect $200");
				p.balance += 200;
				p.location = 0;
				break;
			case 14: 
				System.out.println("Your building and loan matures, collect $150");
				p.balance += 150;
				break;
			case 15:
				System.out.println("Elected chairman of the board, pay each player $50");
				payment = (players.size()-1) * 50;
				if (p.balance < payment) {
					System.out.println("YOU NEED TO RAISE AT LEAST " + (payment - p.balance) + " BY MORTGAGING PROPERTIES, SELLING HOUSES, OR TRADING OR YOU WILL BE ELIMINATED");
					raiseMoney(p, players, board); // this method is going to allow you to trade or mortgage/sell your properties to get out of debt. 
					if (p.balance < payment) {
						p.eliminate = true;
						p.eliminator = " ";
						return;
					}
				}
				for (Player player : players) {
					if (!player.name.contentEquals(p.name)) {
						player.balance += 50;
					}
				}
				p.balance -= payment;
				break;
			case 16:
				System.out.println("Advance to Illinois Avenue");
				p.location = 24;
				break;
		}
	}
	
	public static void goToJail(Player p) {
		p.location = 10;
		p.jail = true;
	}
	
	public static void luxuryTax(Player p, ArrayList<Player> players, ArrayList <Space> board) {
		System.out.println("Pay $100");
		if (p.balance < 100) {
			System.out.println("YOU NEED TO RAISE AT LEAST " + (100 - p.balance) + " BY MORTGAGING PROPERTIES, SELLING HOUSES, OR TRADING OR YOU WILL BE ELIMINATED, if you fail to raise " + (100-p.balance) + ", you will be eliminated");
			raiseMoney(p, players, board); // this method is going to allow you to trade or mortgage/sell your properties to get out of debt. 
			if (p.balance < 100) {
				p.eliminate = true;
				p.eliminator = " ";
				return;
			}
		}
		p.balance -= 100;
	}
	
	public static void raiseMoney(Player p, ArrayList <Player> players, ArrayList <Space> board) {
		Scanner scan = new Scanner(System.in);
		printInfo(p, board);
		String input = "";
		while (true) {
			System.out.println();
			System.out.println("Press t to trade");
			System.out.println("Press m to mortgage");
			System.out.println("Press s to sell houses and hotels");
			System.out.println("Press e to exit");
			
			input = scan.next();
			if (input.equals("t")) {
				trade(p,players,board);
			}
			if (input.equals("m")) {
				mortgage(p,board); // Grab all the eligible properties (properties part of monopolies owned by the player), print them with a number next to them and if the user inputs the number it adds a house to that property
			}
			if (input.equals("e")) {
				 return;
			}
			if (input.equals("s")) {
				sellHouses(p, board);
			}
			else {
				continue;
			}
		}
	}
	
	public static void sellHouses(Player p, ArrayList <Space> board) {
		// print out all the properties with houses, print the value of each of the houses
		Scanner scan = new Scanner(System.in);
		ArrayList <Space> props = new ArrayList();
		for (Space s : board) {
			if (s.type == 'p' && s.owner.equals(p.name) && s.houses != 0) {
				props.add(s);
			}
		}
		if (props.size() == 0) {
			System.out.println("You don't have any properties on which you can sell houses");
			return;
		}
		System.out.println("Properties with houses/hotels");
		for (int i = 0; i<props.size(); i++) {
			System.out.print(i+1 + ". " + props.get(i).name + ". Sell Price for houses: " + .5 * props.get(i).priceHouses); 
			if (props.get(i).houses == 5){
				System.out.println(". Hotels: 1");	
			}
			else {
				System.out.println(". Houses: " + props.get(i).houses);
			}
		}
		int input = 0;
		while (true) {
			System.out.println("Pick the number of the corresponding property you'd like to sell a house/hotel of");
			if (!scan.hasNextInt()){
				System.out.println("Not an integer");
				scan.next();
				continue;
			}
			else {
				input = scan.nextInt();
				if (input > props.size() || input < 1) {
					System.out.println("Input out of bounds");
					continue;
				}
				for (Space prop : board) {
					if (prop.type == 'p' && prop.color.contentEquals(props.get(input-1).color) && prop.houses > props.get(input-1).houses ) {
						System.out.println("You can't sell houses on this property until you have sold houses on the other " + props.get(input-1).color + " properties");
						return;
					}
				}
				break;
			}
		}
		props.get(input-1).houses--;
		p.balance += (.5 * props.get(input-1).priceHouses);
		System.out.println("Your balance is now " + p.balance);
		return;	 
	}
	
	public static void mortgage(Player p, ArrayList <Space> board) {
		Scanner scan = new Scanner(System.in);
		// Print out all of the properties, railroads and utilities owned by player p. 
		// Make an arraylist filled with all those properties, then print them out with their mortgage value and number of houses. 
		ArrayList <Space> props = new ArrayList();
		for (Space s : board) {
			if (s.type != 'a' && s.owner.equals(p.name) && (s.rent != 0 || !s.mortgage)) {
				props.add(s);
			}
		}
		if (props.size() == 0) {
			System.out.println("You don't have any properties you can mortgage");
			return;
		}
		System.out.println("Mortgagable properties");
		for (int i = 0; i<props.size(); i++) {
			System.out.print(i+1 + ". " + props.get(i).name + ". Mortgage value: " + (.5 * props.get(i).cost)); 
			if (props.get(i).type == 'p'){
				System.out.println(". Houses: " + props.get(i).houses);	
			}
			else {
				System.out.println();
			}
		}
		System.out.println("Pick the number of the corresponding property you'd like to mortgage");
		int input = 0;
		while (true) {
			if (!scan.hasNextInt()){
				System.out.println("Not an integer");
				continue;
			}
			else {
				input = scan.nextInt();
				break;
			}
		}
		if (props.get(input-1).type == 'p' && props.get(input-1).houses != 0) {
			System.out.println("This property has houses on it. Would you like to sell all the houses for " + (.5 * (props.get(input-1).houses * props.get(input-1).priceHouses)) + " before mortgaging your property. y or n");
			String response = scan.next();
			if (response.contentEquals("y")) {
				p.balance += (.5 * (props.get(input-1).houses * props.get(input-1).priceHouses));
				props.get(input-1).houses = 0;
			}
			else {
				return;
			}
		}
		p.balance += props.get(input-1).cost * .5;
		props.get(input - 1).rent = 0;
		props.get(input-1).mortgage = true;
		System.out.println("Your new balance is " + p.balance);
	}
	
	public static void incomeTax(Player p, ArrayList <Player> players, ArrayList <Space> board) {
		System.out.println("Pay $200");
		if (p.balance < 200) {
			System.out.println("YOU NEED TO RAISE AT LEAST " + (200 - p.balance) + " BY MORTGAGING PROPERTIES, SELLING HOUSES, OR TRADING OR YOU WILL BE ELIMINATED");
			raiseMoney(p, players, board); // this method is going to allow you to trade or mortgage/sell your properties to get out of debt. 
			if (p.balance < 200) {
				p.eliminate = true;
				p.eliminator = " ";
				return;
			}
		}
		p.balance -= 200;
	}
	
	public static void utility(Player p, ArrayList <Player> players, Space s, ArrayList <Space> board, int roll, boolean chance) { // last boolean tells us whether the player arrived on a utility from the board or from chance, if he arrived from chance then the rent is automatically 10x the roll, if not it's either 4 or 10 times
		Scanner scan = new Scanner(System.in);
		if (s.owner.equals(p.name)) {
			System.out.println("You own this property");
			return;
		}
		if (s.owner.equals(" ") && p.balance >= s.cost) {
			System.out.println("Would you like to buy this utility for " + s.cost + ", y for yes, n for no");
			String input = scan.next();
			if (input.equals("y")) {
				p.balance -= s.cost;
				s.owner = p.name;
				System.out.println("You are now the owner of " + s.name + ", your balance is " + p.balance);
				return;
			}
			else {
				auction(players,s, board);
				return;
			}
		}
		System.out.println("You rolled a " + roll);
		for (int i = 0; i<players.size(); i++) {
			if (s.owner.equals(players.get(i).name)) { // Find the owner
				int n = 0;
				for (Space space : board) {
					if (space.type == 'u' && space.owner.equals(players.get(i).name)) {
						n++;
					}
				} // Find out how many properties the owner has
				int payment = 4 * roll;
				if (chance || n==2) {
					payment = 10 * roll;
				}
				if (s.mortgage) {
					System.out.println("This property is mortgaged");
					return;
				}
				if (p.balance < payment) {
					System.out.println("YOU NEED TO RAISE AT LEAST " + (payment - p.balance) + " BY MORTGAGING PROPERTIES, SELLING HOUSES, OR TRADING OR YOU WILL BE ELIMINATED");
					raiseMoney(p, players, board); // this method is going to allow you to trade or mortgage/sell your properties to get out of debt. 
					if (p.balance < payment) {
						p.eliminate = true;
						p.eliminator = " ";
						return;
					}
				}
				p.balance -= payment;
				players.get(i).balance += payment;
				System.out.println("Pay " + s.owner + " $" + payment);
				System.out.println("Your balance is now " + p.balance);
							
			}
		}
	}

	public static void railroad(Player p, ArrayList <Player> players, Space s, ArrayList <Space> board, boolean chance) {
		Scanner scan = new Scanner(System.in);
		if (s.owner.equals(p.name)) {
			System.out.println("You own this property");
			return;
		}
		if (s.owner.equals(" ") && p.balance < s.cost) {
			System.out.println("You don't have the money to buy this property, would you like to try to raise " + (s.cost - p.balance) + "? (y or n)");
			String n = scan.next();
			if (n.contentEquals("y")){
				raiseMoney(p, players, board);
			}
			else {
				auction(players, s, board);
				return;
			}
		}
		if (s.owner.equals(" ") && p.balance >= s.cost) {
			System.out.println("Would you like to buy this railroad for " + s.cost + ", y for yes, n for no");
			String input = scan.next();
			if (input.equals("y")) {
				p.balance -= s.cost;
				s.owner = p.name;
				System.out.println("You are now the owner of " + s.name + ", your balance is " + p.balance);
				return;
			}
			else {
				auction(players, s, board);
				return;
			}
		}
		if (s.mortgage) {
			System.out.println("This property is mortgaged");
			return;
		}
		for (int i = 0; i<players.size(); i++) {
			if (s.owner.equals(players.get(i).name)) {
				int n = 0;
				for (int j = 0; j<board.size(); j++) {
					if (board.get(j).type == 'r' && board.get(j).owner.equals(players.get(i).name)) {
						n++;
					}
				}
				int payment = (int)(25 * Math.pow(2, n-1));
				if (chance == true) {
					payment *= 2;
				}
				if (p.balance < payment) {
					System.out.println("YOU NEED TO RAISE AT LEAST " + (payment - p.balance) + " BY MORTGAGING PROPERTIES, SELLING HOUSES, OR TRADING OR YOU WILL BE ELIMINATED");
					raiseMoney(p, players, board); // this method is going to allow you to trade or mortgage/sell your properties to get out of debt. 
					if (p.balance < payment) {
						p.eliminate = true;
						p.eliminator = " ";
						return;
					}
				}
				System.out.println("Pay " + payment + " to " + s.owner);
				players.get(i).balance += payment;
				p.balance -= payment;
				System.out.println("Your balance is now " + p.balance);
				return;			
			}
		}
		
	}

	public static void property(Player p, ArrayList <Player> players, Space s, ArrayList <Space> board) {
		Scanner scan = new Scanner(System.in);
		if (s.owner.equals(p.name)) {
			System.out.println("You own this property");
			return;
		}
		if (s.owner.equals(" ") && p.balance < s.cost) {
			System.out.println("You don't have the money to buy this property, would you like to try to raise " + (s.cost - p.balance) + "? (y or n)");
			String n = scan.next();
			if (n.contentEquals("y")){
				raiseMoney(p, players, board);
				if (p.balance < s.cost) {
					auction(players,s,board);
					return;
				}
			}
			else {
				auction(players,s,board);
				return;
			}
		}
		if (s.owner.equals(" ") && p.balance >= s.cost) {
			System.out.println("Would you like to buy " + s.name + " for " + s.cost + "?, y for yes, n for no");
			String input = scan.next();
			if (input.equals("y")) {
				p.balance -= s.cost;
				s.owner = p.name;
				System.out.println("You are now the owner of " + s.name + ", your balance is now " + p.balance);
				monopoly(players, board);
			}
			else {
				auction(players, s, board);
			}
			return;
		}
		for (Player player : players) {
			if (s.owner.equals(player.name)) {
				if (p.balance < s.rent) {
					System.out.println("YOU NEED TO RAISE AT LEAST " + (s.rent - p.balance) + " BY MORTGAGING PROPERTIES, SELLING HOUSES, OR TRADING OR YOU WILL BE ELIMINATED");
					raiseMoney(p, players, board); // this method is going to allow you to trade or mortgage/sell your properties to get out of debt. 
					if (p.balance < s.rent) {
						p.eliminate = true;
						p.eliminator = player.name;
						return;
					}
				}
				System.out.println("Pay " + s.rent + " to " + s.owner);
				player.balance += s.rent;
				p.balance -= s.rent;
				System.out.println("Your balance is now " + p.balance);
			}
		}
	}
	
	public static void auction(ArrayList <Player> players, Space s, ArrayList <Space> board) {
		Scanner scan = new Scanner(System.in);
		int i = 0;
		int topBid = 0;
		Boolean eliminated = false;
		ArrayList <Player> bidders = new ArrayList();
		for (Player player : players) {
			bidders.add(player);
		}
		System.out.println("Auction");
		while (bidders.size() > 1 ) {
			eliminated = false;
			while (true) {
				System.out.println(bidders.get(i).name + ", make a bid above " + topBid);
				System.out.println("Press r to attempt to raise money through trading, mortgaging, or selling houses");
				System.out.println("Enter 0 to drop out of bidding");
				System.out.println("Enter i to see info");
				System.out.println("Your balance is " + bidders.get(i).balance);
				if (!scan.hasNextInt()) {
					String input = scan.next();
					if (input.contentEquals("r")) {
						raiseMoney(bidders.get(i), players, board);
					}
					if (input.contentEquals("i")) {
						printInfo(bidders.get(i), board);
					}
					else {
						System.out.println("Enter an integer or r or i");
					}
					continue;
				}
				int bid = scan.nextInt();
				if (bid > topBid && bid <= bidders.get(i).balance) {
					topBid = bid;
					break;
				}
				else if (bid == 0){
					bidders.remove(i);
					eliminated = true;
					break;
				}
				else if (bid < topBid) {
					System.out.println("Your bid must be above " + topBid);
					continue;
				}
				else {
					System.out.println("You don't have enough money for this bid");
					continue;
				}
			}
			if (!eliminated) {
				i = (i+1) % bidders.size();
			}
			else if (i == bidders.size()) {
				i = 0;
			}
		}
		if (bidders.get(0).balance >= topBid) {
			System.out.println(bidders.get(0).name + " has bought " + s.name + " for " + topBid);
			s.owner = bidders.get(0).name;
			bidders.get(0).balance -= topBid;
		}
		else {
			System.out.println(bidders.get(0).name + " no longer has enough money to purchase " + s.name + ", restart auction");
			auction(players, s, board);
		}
		
	}
	
	public static void build(Player p, ArrayList <Space> board) {
		Scanner scan = new Scanner(System.in);
		ArrayList <Space> props = new ArrayList();
		for (Space space : board) {
			if (space.monopoly && space.owner.contentEquals(p.name)) {
				props.add(space);
			}				
		}
		if (props.size() == 0) {
			System.out.println("You have no eligible properties");
			return;
		}
		else {
			for (int i = 0; i<props.size(); i++) {
				String color = props.get(i).color;
				System.out.println(color);
				while (i<props.size() && props.get(i).color.contentEquals(color)) {
					System.out.print(i+1 + ". " + props.get(i).name);
					if (props.get(i).houses != 5) {
						System.out.print(". Houses: " + props.get(i).houses);
					}
					else {
						System.out.print(". Hotels: 1");
					}
					System.out.print(". Price to build: " + props.get(i).priceHouses);
					System.out.print(". Current rent: " + props.get(i).rent);
					if (props.get(i).houses != 5) {
						System.out.print(". Next rent: " + props.get(i).rents[props.get(i).houses+2]);
					}
					System.out.println();
					i++;
				}
			}
		}
		System.out.println("Press the number corresponding to the property you would like to build on, press 0 to exit build");
		if (!scan.hasNextInt()) {
			scan.next();
			return;
		}
		int input = scan.nextInt();
		if (input == 0) {
			return;
		}		
		else {
			ArrayList <Space> samecolor = new ArrayList();
			for (Space space : board) {
				if (space.type == 'p' && space.color.contentEquals(props.get(input-1).color)) {
					samecolor.add(space);
				}
			}
			Boolean build = true;
			for (Space space : samecolor) {
				if (space.houses < props.get(input-1).houses) {
					build = false;
				}			
			}
			if (!build) {
				System.out.println("You can't build on a property until all of the other same-colored properties have at least the same number of houses");
				return;
			}
			if (p.balance >= props.get(input-1).priceHouses) {
				if (props.get(input-1).houses!=5) {
					p.balance -= props.get(input-1).priceHouses;
					props.get(input-1).houses++;
					updateRent(props.get(input-1));
					if (props.get(input-1).houses == 5) {
						System.out.println("You just built a hotel on " + props.get(input-1).name + ", its rent is now " + props.get(input-1).rent);
					}
					else {
						System.out.println("You just built a house on " + props.get(input-1).name + ", its rent is now " + props.get(input-1).rent);
					}
				}
				else {
					System.out.println("That property already has a hotel on it");
				}
			}
			else {
				System.out.println("You don't have enough money");
				return;
			}
		}
		
	}
	
	public static void printProp(Player p, ArrayList <Space> board) {
		ArrayList <Space> properties = new ArrayList();
		for (int i = 0; i<board.size(); i++) {
			if (board.get(i).type != 'a') {
				if (board.get(i).owner.equals(p.name)){
					properties.add(board.get(i));
				}
			}
		}
		System.out.println("Properties:");
		System.out.println();
		for (int i = 0; i<properties.size(); i++) {
			if (properties.get(i).type != 'p') {
				System.out.print(properties.get(i).name);
				if (properties.get(i).mortgage) {
					System.out.println("(mortgaged)");
				}
				else {
					System.out.println();
				}
			}
			if (properties.get(i).type == 'p') {
				System.out.print(properties.get(i).name);
				System.out.print(" (" + properties.get(i).color + ")");
				if (properties.get(i).rent == 0) {
					System.out.print("(mortgaged)");
				}
				System.out.print("; cost: " + properties.get(i).cost);
				if (properties.get(i).houses == 5) {
					System.out.println("; hotels: 1");
				}
				else {
					System.out.println("; houses: " + properties.get(i).houses);
				}
			}
		}
		int blue = 0;
		int green = 0;
		int yellow = 0;
		int lightBlue = 0;
		int brown = 0;
		int pink = 0; 
		int orange = 0;
		int red = 0; 
		int railroad = 0;
		int utilities = 0;
		System.out.println();
		System.out.println("Monopolies:");
		System.out.println();
		for (Space property : properties) {
			if (property.type == 'r') {
				railroad++;
			}
			else if (property.type == 'u') {
				utilities++;
			}
			else if (property.color.equals("blue")) {
				blue++;
			}
			else if (property.color.equals("green")) {
				green++;
			}
			else if (property.color.equals("yellow")) {
				yellow++;
			}
			else if (property.color.equals("light blue")) {
				lightBlue++;
			}
			else if (property.color.equals("brown")) {
				brown++;
			}
			else if (property.color.equals("pink")) {
				pink++;
			}
			else if (property.color.equals("orange")) {
				orange++;
			}
			else if (property.color.equals("red")) {
				red++;
			}
		}
		if (blue == 2) {
			System.out.println("Blue");
		}
		if (brown == 2) {
			System.out.println("Brown");
		}
		if (lightBlue == 3) {
			System.out.println("Light Blue");
		}
		if (pink == 3) {
			System.out.println("Pink");
		}
		if (orange == 3) {
			System.out.println("Orange");
		}
		if (red == 3) {
			System.out.println("Red");
		}
		if (yellow == 3) {
			System.out.println("Yellow");
		}
		if (green == 3) {
			System.out.println("Green");
		}
		if (railroad == 4) {
			System.out.println("Railroad");
		}
		if (utilities == 2) {
			System.out.println("Utilities");
		}
		System.out.println();
	}
	
	public static void beforeRoll(Player p, ArrayList <Player> players, ArrayList <Space> board) {
			Scanner scan = new Scanner(System.in);
			while (true) {
				System.out.println();
				System.out.println("Press i to receive info on properties");
				System.out.println("Press b to build");
				System.out.println("Press t to trade, mortgage, or sell houses");
				System.out.println("Press u to unmortgage");
				System.out.println("Press r to roll");
				String input = scan.next();
				if (input.equals("i")) {
					printProp(p,board);
					//printInfo(p,board);
				}
				if (input.equals("b")) {
					build(p,board); // Grab all the eligible properties (properties part of monopolies owned by the player), print them with a number next to them and if the user inputs the number it adds a house to that property
				}
				if (input.contentEquals("r")) {
					return;
				}
				if (input.equals("t")) {
					raiseMoney(p,players,board);
					monopoly(players, board);
				}
				if (input.equals("u")) {
					unmortgage(p, board);
				}
				else {
					continue;
				}
			}
		
	}
	
	public static void updateRent(Space s) {
		if (s.monopoly == false){
			s.rent =  s.rents[0];
		}
		else {
			s.rent = s.rents[s.houses+1];
		}
	}
	
	public static void jail(Player p, ArrayList <Player> players, ArrayList <Space> board) {
		Scanner scan = new Scanner(System.in);
		System.out.println("You are in Jail");		
		while (true) {
			System.out.println("Press i to receive info on properties, location, and balance");
			System.out.println("Press b to build");
			if (p.balance >= 50) {
				System.out.println("Press p to pay $50 to get out of jail and roll");
			}
			if (p.getOutOfJail) {
				System.out.println("Press c to use your get out of jail card and roll");
			}
			System.out.println("Press t to trade, mortgage properties, or sell houses");
			System.out.println("Press r to roll, doubles gets you out of jail");
			String input = scan.next();
			if (input.contentEquals("p") && p.balance >= 50) {
				p.balance -= 50;
				p.jail = false;
				return;
			}
			if (input.equals("c") && p.getOutOfJail) {
				p.getOutOfJail = false;
				p.jail = false;
				return;
			}
			if (input.equals("t")) {
				raiseMoney(p, players, board);
			}
			if (input.equals("r")) {
				return;
			}
			if (input.equals("i")) {
				printInfo(p,board);
			}
			if (input.equals("b")) {
				build(p,board);
			}
		}
		
		
	}

	public static void monopoly(ArrayList <Player> players, ArrayList <Space> board) {
		for (Player player : players) {
			for (Space s : board) {
				if (s.type == 'p' && s.owner.contentEquals(player.name)) {
					s.monopoly = true;
					for (Space s2 : board) {
						if (s2.type == 'p' && s2.color.contentEquals(s.color) && !s2.owner.contentEquals(player.name)) {
							s.monopoly = false;
						}
					}
				}
				if (s.type == 'p') {
					updateRent(s);
				}
			}
		}
		
	}

	public static void trade(Player p, ArrayList <Player> players, ArrayList <Space> board) {
		Scanner scan = new Scanner(System.in);
		System.out.println();
		for (Player player : players) {
			if (p != player) {
				System.out.println(player.name);
				printInfo(player, board);
			}
		}
		Player trader = new Player("ghjlas");
		while (true) {
			System.out.println("Who do you want to trade with? (Enter anything besides a name to exit trade menu)");
			String traderName = scan.next();
			for (Player player : players) {
				if (!traderName.contentEquals(p.name) && traderName.contentEquals(player.name)) {
					trader = player;
				}
			}
			if (trader.name.contentEquals("ghjlas") || trader.name.contentEquals(p.name)) {
				return;
			}
			else {
				break;
			}
		}
		// So I need to print out each of the assets of the player I want to trade with with numbers attached to them.
		ArrayList <Space> traderProps = new ArrayList();
		for (Space space : board) {
			if (space.type != 'a' && space.owner.contentEquals(trader.name)) {
				traderProps.add(space);
			}
		}
		System.out.println();
		ArrayList <Space> yourProps = new ArrayList();
		for (Space space : board) {
			if (space.type != 'a' && space.owner.contentEquals(p.name)) {
				yourProps.add(space);
			}
		}
		//Make a loop and print i + 1 and then the property name, then each property can be identified by traderprops(input-1)
		System.out.println("Your assets");
		System.out.println("1. Money (" + p.balance + " dollars)");
		for (int i = 0; i < yourProps.size(); i++) {
			if (yourProps.get(i).type == 'p') {
				System.out.println(i+2 + ". " + yourProps.get(i).name + " (" + yourProps.get(i).color + "), cost: " + yourProps.get(i).cost + ", houses: " + yourProps.get(i).houses);
			}
			else {
				System.out.println(i+2 + ". " + yourProps.get(i).name + ", cost: " + yourProps.get(i).cost);
			}
		}
		System.out.println();
		System.out.println(trader.name + "'s assets");
		System.out.println("1. Money (" + trader.balance + " dollars)");
		for (int i = 0; i < traderProps.size(); i++) {
			if (traderProps.get(i).type == 'p') {
				System.out.println(i+2 + ". " + traderProps.get(i).name + " (" + traderProps.get(i).color + "), cost: " + traderProps.get(i).cost + ", houses: " + traderProps.get(i).houses);
			}
			else {
				System.out.println(i+2 + ". " + traderProps.get(i).name + ", cost: " + traderProps.get(i).cost);
			}
		}
		System.out.println();
		System.out.println("Note that trading properties with houses on them will automatically sell all houses to the bank");
		System.out.println();
		ArrayList <Space> yourOffer = new ArrayList();
		int yourOfferAmount = 0;
		while (true) {
			System.out.println("Enter the corresponding number for the asset you would like to offer for trade, enter 0 when you've entered all the properties, c to cancel trade");
			if (!scan.hasNextInt()) {
				System.out.println("Please enter an integer");
				String input = scan.next();
				if (input.contentEquals("c")) {
					return;
				}
			}
			int input = scan.nextInt();
			if (input == 1) {
				System.out.println("How much would you like to offer");
				if (scan.hasNextInt()) {
					int amount = scan.nextInt();
					if (amount < 0 || amount > p.balance) {
						System.out.println("Amount is out of bounds");
						continue;
					}
					else {
						yourOfferAmount = amount;
					}
				}
				else {
					System.out.println("Not an integer");
					continue;
				}	
			}
			else if (input - 2 >= yourProps.size() || input < 0) {
				System.out.println("Input out of bounds");
				continue;
			}
			else if (input == 0) {
				break;
			}
			else {
				yourOffer.add(yourProps.get(input-2));
			}
		}
		int traderOfferAmount = 0;
		ArrayList <Space> traderOffer = new ArrayList();
		while (true) {
			System.out.println("Enter the corresponding number for the asset you would like to receive, enter 0 when you've entered all the properties, c to cancel trade");
			if (!scan.hasNextInt()) {
				String input = scan.next();
				if (input.contentEquals("c")) {
					return;
				}
				System.out.println("Please enter an integer");
				continue;
			}
			int input = scan.nextInt();
			if (input == 1) {
				System.out.println("How much would you like to receive");
				if (scan.hasNextInt()) {
					int amount = scan.nextInt();
					if (amount < 0 || amount > trader.balance) {
						System.out.println("Amount is out of bounds");
						continue;
					}
					else {
						traderOfferAmount = amount;
					}
				}
				else {
					System.out.println("Not an integer");
					continue;
				}	
			}
			else if (input - 2 >= traderProps.size() || input < 0) {
				System.out.println("Not in range");
				continue;
			}
			else if (input == 0) {
				break;
			}
			else {
				traderOffer.add(traderProps.get(input-2));
			}
			
		}
		System.out.println(p.name + "'s offering:");
		System.out.println();
		if (yourOfferAmount != 0) {
			System.out.println(yourOfferAmount + " dollars");
		}
		for (Space prop : yourOffer) {
			System.out.println(prop.name);
		}
		System.out.println();
		System.out.println("For:");
		System.out.println();
		if (traderOfferAmount != 0) {
			System.out.println(traderOfferAmount + " dollars");
		}
		for (Space prop : traderOffer) {
			System.out.println(prop.name);
		}
		System.out.println();
		System.out.println(trader.name + ", do you accept this trade? (y or n)");
		String input = scan.next();
		if (input.contentEquals("y")){
			p.balance += traderOfferAmount;
			p.balance -= yourOfferAmount;
			for (Space prop : yourOffer) {
				prop.monopoly = false;
				prop.owner = trader.name;
				if (prop.type == 'p') {
					trader.balance += (prop.houses * prop.priceHouses)/2;
					prop.houses = 0;
					updateRent(prop);
				}				
			}
			trader.balance += yourOfferAmount;
			trader.balance -= traderOfferAmount;
			for (Space prop : traderOffer) {
				prop.monopoly = false;		
				prop.owner = p.name;
				if (prop.type == 'p') {
					trader.balance += (prop.houses * prop.priceHouses)/2;
					prop.houses = 0;
					updateRent(prop);
				}
			}
			System.out.println("The trade has been made");
		}
		else {
			System.out.println("Trade cancelled");
		}
	}
}
