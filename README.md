# Checkers

============
Disclaimer:
============

This project has been designed and implemented as a part of the requirements for CS-499 Senior Design Project for Spring 2016 semester. 
While the authors make every effort to deliver a high quality product, we do not guarantee that our products are free from defects. 
Our software is provided "as is," and you use the software at your own risk.
We make no warranties as to performance, merchantability, fitness for a particular purpose, or any other warranties whether expressed or implied.
No oral or written communication from or information provided by the authors or the University of Kentucky shall create a warranty.
Under no circumstances shall the authors or the University of Kentucky be liable for direct, 
	indirect, special, incidental, or consequential damages resulting from the use, misuse, or inability to use this software, 
	even if the authors or the University of Kentucky have been advised of the possibility of such damages



============================================================
CS 499 Senior Design Project, Team 13 "Chromecast Checkers":
============================================================

Folder Contents:

	Final Deliverables/
		
		Android APK/
			ChromecastCheckersFinal.apk											-Final installer file for the Android Companion application.
		
		Code Files/
			Android Code/
				MainActivity.java												-Main source code for the Android Companion application
			Chromecast Server Code/ 
				angular.min.js													-Dependency for receiver.html, received from: https://docs.angularjs.org/misc/downloading
				ngCheckers.css													-CSS Dependency for receiver.html, sets the look for the checkers board
				receiver.html													-Main source code for the Chromecast Server Application.
		
		Demonstration Videos/
			Chromecast_Checkers_Demonstration.mp4								-Final Video Demonstration of the applications working in conjuction.
			Demonstration_With_a_Bug.mp4										-Video Demonstration with a minor bug, the Android Companion Application was not changing the "Your Turn" indicator. This was fixed, shown in Chromecast_Checkers_Demonstration.mp4
		
		Diagrams and UI Pictures/
			CheckersDoubleJump.png												-PNG File showing final UI for a double jump
			CheckersKings.png													-PNG File showing final UI with Kings
			CheckersPlayer1YourTurn.png											-PNG File showing final UI for Player 1's turn
			CheckersPlayer2Wait.png												-PNG File showing final UI for Player 2, waiting for turn
			CheckersStart.png													-PNG File showing final UI when starting the game
			CheckersWin.png														-PNG File showing final UI when ending the game
			DiagramOne.png														-Simple Diagram showing how the devices/applications all connect
			DiagramTwo.jpg														-More complex diagram on how the devices communicate with one another
			DiagramThree.png													-More complex individual diagrams on how the applications work themselves
			DiagramFour.png														-More complex individual diagrams on how the applications work themselves
			DiagramFive.png														-JSON Diagram on how the JSON messages are deilvered for communication method
			DiagramSix.png														-Google Cast SDK Console example, see below
			potlogo.jpg															-Chromecast Checkers and Android Application Logo
		
		Documentation Files/
			ChromecastCheckersDesign.pdf										-Rough design of project, done by midterm, showing logic and diagram explanations
			FINALpresentation.pdf												-Final slides shown at our presentation to the professor and audience
			presentationMidTerm.pdf												-Slides shown at our presentation to the professor and class at midterm
			Testing.pdf															-Test cases before implementation
			TestingImplementation.pdf											-Test cases after implementation with comments
		
		Website Files/
			*.html																-All HTML Files from https://sites.google.com/site/cs499checkersapp/home - they may appear weird, see live site instead
			README.txt															-README File for the website document. Has a link to live website, https://sites.google.com/site/cs499checkersapp/home
		
		FinalReport.pdf															-Final report documentation with diagrams, UI pictures, explanations, and exposition of project as a whole
		README.txt																-This Current Document


======================
Description and Usage:
======================

Chromecast Checkers and the Android Checkers companion application are simple game applications designed for Chad McQuillan, of Lexmark. 
It is intended to allow users to simply play checkers on their Chromecast using Android devices as controllers. 
The program runs a HTML/Javascript server program that processes everything. This program casts to the Chromecast screen. 
The Android companion applications connect to the server file and tell it to cast and when to tear down. 
The Android application also handles the button input, but validation of the input is handled by the server program.


	receiver.html - To Play:
	
						The UI is pretty simple, there is the 8x8 checker board, 12 “black” pieces and 12 red pieces. 
						Due to the color scheme of the board the “black” pieces are shown as grey, we kept the name black as that is what is traditional in checkers. 
						We show the name, “Chromecast Checkers” at the top. Below the board is an indicator of whose turn it is, and what the scores are. 
						Scores are tallied by pieces jumped. If red has a score of 5, then red has “jumped” or taken 5 black pieces. This is how the status of the game is kept. 
						Once a player has a score of 12, the other player will have no remaining pieces left and will lose.
						The selector is a green highlight square that is moved around by the Android controller and can “select” pieces and squares. 
						When a piece is selected, that piece is highlighted blue. The spaces open to jump to are also highlighted blue. 
						Double jumps can take place, and the player can move elsewhere, take a single jump, or complete the full double jump. 
						Jumps further than a double jump can occur and will show up. Triple jumps have happened and work as intended. 
						Kings are shown with a bold faced K.
					
					To Install/Run:
					
						There is nothing to install on individual Chromecast devices. 
						What one (the Customer, Chad, for example) would need to do is to publish the Chromecast app on the Google Cast SDK Developer Console, see diagram 6. 
						When you publish this application you initialize a place that has the server file hosted. 
						Such as for our team, Matt Dunbar has his own website. We hosted the JavaScript Server file on his website. 
						In his Google Cast SDK Developer Console, we have an application, “Chromecast Checkers”, and this SDK points to the JavaScript server file on his website. 
						Every time a user hits the cast button on the Android App, the SDK knows what application to use (this is hard coded within the companion application), 
						finds where to look for this server file and fetches a copy of it. From here the application is cast to the Chromecast and runs.

	MainActivity.java - To Play:
	
							There are 8 buttons. There are 4 movement buttons, up, down, left, and right. 
							There is a select button, register button and a forfeit button. 
							Finally, there is a small cast button in the upper right hand corner across from the Chromecast Checkers title. 
							To start off, when the player opens the application the first thing to do will be to press the cast button and select the Chromecast they want to play on, 
							usually this will only have one Chromecast. Both players will need to do this and connect to the same Chromecast. 
							Next the players will both press the register button, this will prevent other users from trying to interrupt the game and “steal” access 
							by trying to register their own device. The first player to register will be player 1 and will be red, the last to register will be player 2 and be black. 
							Once the player has connected and has registered, the “Player X” will change to “Player #” to indicate which player they are, 
							“Turn” will change to “Your Turn” or “Waiting for Turn” to indicate if it is their turn or not, and the register button will disappear. 
							At any time, during their turn, a player may forfeit. If the player becomes unable to move pieces they will be forced to forfeit. 

						To Install:
							
							The app can simply be published to the Google Play Store. From here the application can be downloaded by users and be used 
							as normal to connect to a Chromecast and begin casting Checkers. 
							
							Alternatively, download the supplied .APK file and install that on your Android device. 
							This file is what Android uses to install applications. This file is the installer / distribution. 


======
Other:
======

For more in-depth on the game guide or how to use the source code files, please see the final report, sections Google Cast and Game Guide.

For more in-depth on the implementation of the game code, please see the final report, sections Program Specifications, Planning, Design, and Implementation

For in-depth analysis on future enchancements to this project, please see the final report, section Future Enhancements/Maintenance


