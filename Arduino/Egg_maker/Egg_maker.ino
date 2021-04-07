char data = 0;                //Variable for storing received data
boolean start = false;
long start_time;
double duration = 10000;
long last_trigger = millis();

void setup() {
  Serial.begin(9600);         //Sets the data rate in bits per second (baud) for serial data transmission
  pinMode(LED_BUILTIN, OUTPUT);        //Sets digital pin 13 as output pin
}

boolean toggle = true;

void loop(){
  if(Serial.available() > 0){  // Send data only when you receive data:
    data = Serial.read();      //Read the incoming data and store it into variable data
    //Serial.print(data);        //Print Value inside data in Serial monitor
    //Serial.print("\n");        //New line 
    if(data == '1'){            //Checks whether value of data is equal to 1 
      if (!start){
        start = true;
        start_time = millis();
      }
//      digitalWrite(LED_BUILTIN, HIGH);  //If value is 1 then LED turns ON
    }
    else if(data == '0'){       //Checks whether value of data is equal to 0
//      digitalWrite(LED_BUILTIN, LOW);   //If value is 0 then LED turns OFF
    }
  }

  if(start &&  (millis() - start_time) % 500 == 0 && millis() - last_trigger > 10){  // Every 0.5 seconds update us
    last_trigger = millis();
    Serial.print("STATUS:");
    double fraction = ((double)(millis() - start_time))/duration;
    printDouble(fraction, 4);
    toggle = !toggle;
    if(toggle){ // flip LED every 1sec
      digitalWrite(LED_BUILTIN, HIGH);  //LED turns ON
      Serial.println("DEBUG:HIGH");
    }
    else{
      digitalWrite(LED_BUILTIN, LOW);
      Serial.println("DEBUG:LOW");
    }
  }
  if(start & (millis() - start_time) > duration){  // if it has been more than 10 seconds
    start = false;
    Serial.println("STATUS:1.0"); // complete
    Serial.println("DONE");
  }

}

void printDouble( double val, byte precision){
 // prints val with number of decimal places determine by precision
 // precision is a number from 0 to 6 indicating the desired decimial places
 // example: printDouble( 3.1415, 2); // prints 3.14 (two decimal places)
  Serial.print(int(val));  //prints the int part
  if( precision > 0) {
    Serial.print("."); // print the decimal point
    unsigned long frac;
    unsigned long mult = 1;
    byte padding = precision -1;
    while(precision--)
       mult *=10;
     
    if(val >= 0)
      frac = (val - int(val)) * mult;
    else
      frac = (int(val)- val ) * mult;
    unsigned long frac1 = frac;
   while( frac1 /= 10 )
     padding--;
   while(  padding--)
     Serial.print("0");
   Serial.println(frac,DEC) ;
 }
}
