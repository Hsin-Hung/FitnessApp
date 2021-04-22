# CS501FitnessApp

https://docs.google.com/document/d/1RH23igtt_PtkAcdY9t3HWrfhdLL7GjZBpHnW08tlOcY/edit?usp=sharing

Presentation: https://docs.google.com/presentation/d/1XoC30JsssStljB0MHIz284gt46oOuTfb-cJ-SEZC0Jc/edit?ts=605c9592#slide=id.p

Marketing Report: https://docs.google.com/document/d/10yEVwmOK6W7ScaIVBPYl-nrl7oNIK1TaTE6W9pNO41g/edit#heading=h.f7ulxp3sg2vz


# Notes for GRADER when running the app:

1. 
    In Testing Mode, Google only allows test users to use the app with GoogleFit API, which our app does. In order for any gmail to access our app, we will have to publish our app and have an extensive app verification process with google. This provided gmail below is one the test user. 

    Use this gmail to sign in the app with Google Sign In: 

        email: cs501testing@gmail.com	password: cs501e2testing

2. 
    Our app deals with fitness challenge of a set time interval. It has a minimum challenge time of one day, where distance challenge updates participant distance once every 15 minutes and the weight challenge prompts the user for a new weight image once a week. The work manager that manages the background process wouldn't allow the scheduled tasks to have scheduling intervals less than 15 minutes. We have these test buttons that allow you to show how the interval, distance fetching, and end background tasks are done to avoid waiting for 15 minutes or 1 day for the challenge to end. I'll decribe the functionalities of the test buttons below:

        Distance challenge:

            PERIOD button: it starts a background task that fetches the user's distance data from the History client and updates the view, it will be 0 until the recording client record distances. 

            SIMULATE button: it assign random distances to all participants 

            END button: it ends the distance challenge and will guide the user to the challenge ending screen
        
        Weight loss challenge:
            
            PERIOD button: it will prompt the user for new weight image the next time he/she enters the weight challenge 
            
            END button: it ends the weight challenge and will guide the user to the challenge ending screen




