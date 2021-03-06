import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;
/////////////////////////////////////////////////////////////////////////////
//Semester:         CS367 Fall 2017 
//PROJECT:          Program 5 Event-Volunteer Match
//FILE:             VolunteerMatch.java
//
//TEAM:    Benjamin Challe
//Authors: Benjamin Challe, bchalle@wisc.edu, bchalle,002
//
//---------------- OTHER ASSISTANCE CREDITS 
//Persons: none 
//
//Online sources:none
////////////////////////////80 columns wide //////////////////////////////////
/**
 * VolunteerMatch is the main class for this program.
 * 
 * IMPLEMENT SOME METHODS OF THIS CLASS
 * 
 * It provides a main menu loop that allows the user
 * to manage events and volunteers and create (and remove)
 * matches between the two types.
 * 
 * See @EventManager for the type that stores events and volunteers.
 * 
 * The main menu loop interacts with an instance of EventManager using menu.
 * This class is also responsible for the most of the 
 * user input from menu and for reading and writing data in files.
 */
public class VolunteerMatch {
	
	/** Use this scanner to read from prompt*/
	private final static Scanner scn = new Scanner(System.in);

	/**
	 * The main method of this program.
	 * 
	 * THIS METHOD IS IMPLEMENTED FOR YOU
	 * 
	 * NOTE : PRINTING OUT IN THIS PROGRAM ONLY USES PREBUILT STRINGS FROM Resource CLASS.
	 * USE ONLY "System.out.format(str, arg,..)" or "System.out.print(str)"
	 * THE USE OF "System.out.println(str)" MAY CAUSE ADDITIONAL NEW LINES IN YOUR PROGRAM
	 * DO COMPARE TO PROVIDED SAMPLE RUNS TO CHECK WHETHER YOUR OUTPUT HAS ADDITIONAL NEW LINES
	 */
	public static void main(String[] args){
		
		// Use an EventManager to store events and volunteers and matches
		EventManager manager = new EventManager();

		boolean isContinued = true;
		while(isContinued){
			
			System.out.print(Resource.STR_MENU_MAIN);
			String input = scn.nextLine().trim();
			switch(input){
				case "1": { // *** Load EVM from a file *** //
					// Resource.STR_INPUT_FILEPATH
					// Resource.STR_ERROR_READ_FILE_PRINT_FORMAT
					System.out.print(Resource.STR_INPUT_FILEPATH);
					String filePath = scn.nextLine().trim();
					try{
						readFromFile(manager, filePath);
					} catch(FileNotFoundException e){
						System.out.format(Resource.STR_ERROR_READ_FILE_PRINT_FORMAT, filePath);
					}
					break;
				}
				case "2": {// *** Save EVM to a file *** //
					// Resource.STR_INPUT_FILEPATH
					// Resource.STR_ERROR_WRITE_FILE_PRINT_FORMAT
					System.out.print(Resource.STR_INPUT_FILEPATH);
					String filePath = scn.nextLine().trim();
					try{
						writeToFile(manager, filePath);
					} catch(FileNotFoundException e){
						System.out.format(Resource.STR_ERROR_WRITE_FILE_PRINT_FORMAT, filePath);
					}
					break;
				}
				case "3": {// *** Display all events and corresponding matches *** //
					manager.displayAllEvents();
					break;
				}
				case "4": {// *** Display all volunteers and corresponding matches *** //
					manager.displayAllVolunteers();
					break;
				}
				case "5": {// *** Create a match *** //
					// Resource.STR_INPUT_MATCH_EVENT_NAME
					// Resource.STR_INPUT_MATCH_VOLUNTEER_NAME
					// Resource.STR_ERROR_MATCH_CREATE_FAILED_PRINT_FORMAT
					System.out.print(Resource.STR_INPUT_MATCH_EVENT_NAME);
					String eventName = scn.nextLine().trim();
					System.out.print(Resource.STR_INPUT_MATCH_VOLUNTEER_NAME);
					String volunteerName = scn.nextLine().trim();

					if(!manager.createMatch(eventName, volunteerName)){
						System.out.format(Resource.STR_ERROR_MATCH_CREATE_FAILED_PRINT_FORMAT, eventName, volunteerName);
					}

					break;
				}
				case "6": {// *** Remove a match *** //
					// Resource.STR_INPUT_MATCH_EVENT_NAME
					// Resource.STR_INPUT_MATCH_VOLUNTEER_NAME
					// Resource.STR_ERROR_MATCH_REMOVE_FAILED_PRINT_FORMAT
					System.out.print(Resource.STR_INPUT_MATCH_EVENT_NAME);
					String eventName = scn.nextLine().trim();
					System.out.print(Resource.STR_INPUT_MATCH_VOLUNTEER_NAME);
					String volunteerName = scn.nextLine().trim();

					if(!manager.removeMatch(eventName, volunteerName)){
						System.out.format(Resource.STR_ERROR_MATCH_REMOVE_FAILED_PRINT_FORMAT, eventName, volunteerName);
					}
					break;
				}
				case "7": {// *** Add a volunteer *** //
					// Resource.STR_INPUT_VOLUNTEER_NAME
					// Resource.STR_INPUT_VOLUNTEER_AVAILABLE_DATE
					// Resource.STR_ERROR_VOLUNTEER_CREATE_FAILED_PRINT_FORMAT
					System.out.print(Resource.STR_INPUT_VOLUNTEER_NAME);
					String volunteerName = scn.nextLine().trim();
					System.out.print(Resource.STR_INPUT_VOLUNTEER_AVAILABLE_DATE);
					input = scn.nextLine().trim();
					String[] dateStrSplitAry = input.split(",",-1);

					if(!manager.addVolunteer(volunteerName, dateStrSplitAry)){
						System.out.format(Resource.STR_ERROR_VOLUNTEER_CREATE_FAILED_PRINT_FORMAT, volunteerName);
					}

					break;
				}
				case "8": {// *** Remove a volunteer *** //
					// Resource.STR_INPUT_VOLUNTEER_NAME
					// Resource.STR_ERROR_VOLUNTEER_REMOVE_FAILED_PRINT_FORMAT
					System.out.print(Resource.STR_INPUT_VOLUNTEER_NAME);
					String volunteerName = scn.nextLine().trim();

					if(!manager.removeVolunteer(volunteerName)){
						System.out.format(Resource.STR_ERROR_VOLUNTEER_REMOVE_FAILED_PRINT_FORMAT, volunteerName);
					}
					break;
				}
				case "9": {// *** Quit *** //
					isContinued = false;
					break;
				}
				default :
					// Resource.STR_ERROR_MENU_INPUT
					System.out.print(Resource.STR_ERROR_MENU_INPUT);
			}
		}
	}

	/**
	 * Read data input file and parse to add volunteers and events into event manager.
	 * 
	 * Note: Volunteers are read first, so that they exist before reading events that contain volunteer lists
	 * Events may or may not include volunteers that have already been matched.
	 * If a volunteer is listed for an event, that means that the volunteer is available on that date.
	 * 
	 * (Volunteer Line Format)
	 *   v;{name};{date},{date}...
	 * (Event Line Format)
	 *   e;{name};{date};{limit};{volunteer},{volunteer}...
	 * 
	 * (Valid Volunteer Line Example)
	 *   v  ;Mingi ;1 ,2  ,3, 4,23
	 *   V: Sonu;
	 * (Valid Event Line Example)
	 *   e   ;Birth Day;23  ;1;  Mingi ,Sonu // should have Mingi and Sonu added into manager.
	 *   E;birthday  ; 23; 10;
	 * 
	 * NOTE1 : ignore lines that have invalid format and continue to parse
	 * NOTE2 : there is no certain order for v/e lines but matched volunteers for an event must be added before adding the event.
	 * 
	 * @see P5 description on Canvas
	 * 
	 * @param manager an EventManager instance
	 * @param filePath a VE file path to read
	 * @throws FileNotFoundException if a file is not in filePath, it throws FileNotFoundException
	 */
	public static void readFromFile(EventManager manager, String filePath) throws FileNotFoundException{
		File inputFile = new File(filePath);  //create a new file
		Scanner fileScn = new Scanner(inputFile); //scan the file
		while (fileScn.hasNextLine()) { //while there is another line
			String line = fileScn.nextLine(); //save the line
			String[] lineSplit = line.split(";"); //split at ;
			if (lineSplit[0].equalsIgnoreCase("e")) { //if the code is e
				if(lineSplit.length >= 4){ //and if it is properly formated
				manager.addEvent(lineSplit[1].trim(), lineSplit[2].trim(), lineSplit[3].trim()); //add a new event
				if (lineSplit.length > 4) { //if there are volunteers
					String[] names = lineSplit[4].split(","); //get the list of names
					for (int i = 0; i < names.length; i++) { //for each name
						manager.createMatch(lineSplit[1].trim(), names[i].trim()); //create a match for each name
					}
				}
				}
			} else if (lineSplit[0].equalsIgnoreCase("v")) { //if the code is for volunteers
				String[] dates = lineSplit[2].split(","); //get the dates
				try{
				manager.addVolunteer(lineSplit[1], dates); //try to add the volunteer
				}catch(IllegalArgumentException e){ //if you cant skip it
					//skip
				}
			}
			//TODO: implement this to handle the menu items
		}
		fileScn.close(); //close the scanner
	}

	/**
	 * Write volunteers and events to a file. Writes volunteers first and then events.
	 * The events include any volunteers that have already been matched.
	 * 
	 * (Volunteer Line Format)
	 *   v;{name};{date},{date}...
	 * (Event Line Format)
	 *   e;{name};{date};{limit};{volunteer},{volunteer}...
	 *   
	 * (Example)
	 * v;Mingi;10,15,20
	 * v;Sonu;11,16,20
	 * e;Birthday;20;3;Mingi,Sonu
	 * e;Field trip;3;6;
	 * 
	 * NOTE : there is no additional new line at the end
	 * 
	 * @param manager an EventManager instance that can track volunteers and events
	 * @param filePath the name of a file to write date toe
	 * @throws FileNotFoundException if the program cannot make a file to the filePath, it throws FileNotFoundException
	 */
	public static void writeToFile(EventManager manager, String filePath) throws FileNotFoundException{
	
		PrintWriter writer = new PrintWriter(filePath); //create a new print writer
		writer.print(manager.toStringAllVolunteers()); //print the volunteers
		writer.print(manager.toStringAllEvents()); //print the events
		//TODO: implement this to handle the menu items

		writer.close(); //close the printer
	}
}
