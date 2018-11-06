package usefulJavaElementsForABM;

import java.util.Scanner;

public class searchWithinString {

	public static void main(String[] args) {
		
		// Goal: search if a given text is in the a text
		
		// [1] Get the search text (input by the user)
		Scanner scanner = new Scanner(System.in);
		System.out.printf("What are you searching?\n");
		String needle = scanner.nextLine();
		System.out.printf("You are searching for: %s\n",needle);
		
		
		// [2] Adjust the search string to increase matches (if desired only)
		
		needle = needle.trim().toLowerCase();
					// trim() removes leading and ending whitespaces
					// toLowerCase() makes all lower case: do not use this if your search should be case sensitive
		
		// [3] Search within the text to be searched (also converted to lower case)
		String haystack = "I want to search within this text for some specific elements";
		boolean textFound = haystack.toLowerCase().contains(needle);
		
		// [4] Output the result
		if(textFound) {
			System.out.printf("Yes, I found '%s' within the text\n",needle);
		}
		else {
			System.out.printf("Sorry, there was no match for '%s'\n",needle);
		}
		
		
		
	}

}
