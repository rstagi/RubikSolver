#include <SoftwareSerial.h>
#include <SoftwareServo.h> 

class ServoMotore{
  public:
    ServoMotore(int pin, int angoloA, int angoloB, int angoloO){
      this->pin = pin;
      servo.attach(pin);
      this->angoloA = angoloA; //angolo per movimento Antiorario
      this->angoloB = angoloB; //angolo Base
      this->angoloO = angoloO; //angolo per movimento Orario
      ruota(0); //raggiunge la posizione base
    }
    void ruota(int dir){ 
       switch(dir){
         case -1: muovi(angoloA);  break; //movimento Antiorario
         case 0: muovi(angoloB);  break; //Base
         case 1: muovi(angoloO); break; //Orario
         default: Serial.println("Dir sconosciuta in ServoMotore::ruota(). Dir="+dir);
       }
    }
    void muovi(int angolo){ //movimento all'angolo passato per parametro
      int pos = servo.read(), speed = (pos>angolo)?-1:1; //posizione attuale e direzione
      for(bool refresh=false; pos!=angolo; pos+=speed, refresh=!refresh){
        if(refresh) SoftwareServo::refresh(); //refresh del Servo, necessario almeno ogni 20ms
        servo.write(pos);
        delay(10);
      }
    }
    
    void setupS(){
      ruota(0);
    }
    
    int getPin(){ return pin; }
    
    int getPos(){
      return servo.read();
    }
    
  private:
    SoftwareServo servo;
    int pin,angoloA,angoloB,angoloO,inc;
};

class MotoreDC{
  public:
    MotoreDC(int pin1, int pin2, int tempo){
      this->pin1 = pin1; //settaggio attributi
      this->pin2 = pin2;
      this->tempo = tempo;
      pinMode(pin1, OUTPUT); //settaggio a OUTPUT dei pin
      pinMode(pin2, OUTPUT);
      setupM(); //posizione aperta
      //per essere sicuri lo si fa aprire per più tempo del dovuto
    }
    void apri(){ //metodo per aprire la mano
        digitalWrite(pin1, HIGH);
        digitalWrite(pin2, LOW);
        delay(tempo);
        stopM();
    }
    void chiudi(){ //metodo per chiudere la mano
        digitalWrite(pin1, LOW);
        digitalWrite(pin2, HIGH);
        delay(tempo);
        stopM();
    }
    void stopM(){ //metodo per fermare il movimento
      digitalWrite(pin1, LOW);
      digitalWrite(pin2, LOW);
    }
    
    void setupM(){
      digitalWrite(pin1, HIGH);
      digitalWrite(pin2, LOW);
      delay(tempo+200);
      stopM();
    }
    
  private:
    int pin1, pin2, tempo;
};

ServoMotore* polso[2];
MotoreDC* mano[2];
SoftwareSerial BT(4,3);

char c;
int cont=0;
char comando[100];
bool prendiMosse, eseguiMosse;

void eseguiMossa(char mossa){
  switch(mossa){
    case 'l':
      mano[1]->apri();
      break;
    case 'L':
      mano[0]->apri();
      break;
    case 'b':
      polso[1]->ruota(0);
      break;
    case 'B':
      polso[0]->ruota(0);
      break;
    case 'p':
      mano[1]->chiudi();
      break;
    case 'P':
      mano[0]->chiudi();
      break;
    case 'a':
      polso[1]->ruota(-1);
      break;
    case 'A':
      polso[0]->ruota(-1);
      break;
    case 'o':
      polso[1]->ruota(1);
      break;
    case 'O':
      polso[0]->ruota(1);
      break;
  }
}


void setup() {
  Serial.begin(9600);
  BT.begin(115200);
  BT.print("$$$");
  delay(500);
  BT.println("U,9600,N");
  delay(300);
  BT.begin(9600);
  prendiMosse=true;
  eseguiMosse=false;
  
  mano[0] = new MotoreDC(6, 7, 400);
  mano[1] = new MotoreDC(10, 11,400);
  polso[0] = new ServoMotore(15,180, 96, 11, 12);
  polso[1] = new ServoMotore(17, 180, 94, 11, 12);
  
  mano[0]->chiudi();
}

void loop() {
  if(prendiMosse && BT.available()){
    c = (char)BT.read();
    comando[cont++] = c;
    if(c=='.'){
      eseguiMosse=true;
      prendiMosse=false;
    }
    Serial.print(c);
  }else if(eseguiMosse){
    for(int i=0; i<cont && comando[i]!='.'; i++) //for per vedere le mosse da fare ed eseguirle
      eseguiMossa(comando[i]);
    eseguiMosse=false;
    prendiMosse=true;
    BT.write("OKOKOK");
    cont=0;
  }
  delay(25);
}
