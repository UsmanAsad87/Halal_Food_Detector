package com.projectai.halalfooddetector;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class RecognizeTextAndCheckFood extends AppCompatActivity {

    // These list data structures are used to read data from CSV files
    private List<EcodeSample> data= new ArrayList<>(); //it will save ecodes,name and status from data file
    private List<Food> foodData= new ArrayList<>(); // it will read name of food and its status from foodbook file
    private List<Food> allergy= new ArrayList<>(); // it will read the allergies corresponding to food ingredients

    private String IngredientsWithEcodes;
    private String IngredientsWithoutEcodes;

    private String HaramIngredientsWithEcodes;
    private String HaramIngredientsWithoutEcodes;

    private boolean check=false;
    private boolean check2=false;
    private int dataLength=0;
    private int allergyLength=0;
    private int foodDataLength=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recog);

        // Get the extracted string from ExtractText.java
        Bundle bundle = getIntent().getExtras();
        String message = bundle.getString("message");

        //to save it from Application failure
        message=message+"No data is scanned";


        // reads the files
        readEcodes();
        readAllergy();
        readFood();




        //get the allergies corresponding to the ingredients
        String Allergies=getAllergies(message);

        //takes the whole string and return Ecodes only
        String ecodes = giveEcodes(message);

        //Check if food is halal or haram based on ecodes
        Boolean status= checkFood(ecodes);  //status corresponding to ecodes

        //it will save the name of ingredients corrosponding to ecodes to global string IngredientsWithEcodes
        getname(ecodes);

        //Check if food is halal or haram based on ingredients
        Boolean foodStatus=checkFoodData(message); //status corresponding to name of ingredients
        Boolean status_food= status && foodStatus;

        //it will save the name of ingredients corrosponding to ecodes to global string IngredientsWithEcodes
        getname(ecodes);

        //it will save the ecode and names of haram ingredeints to HaramIngredientsWithEcodes
        haramIngFinder(ecodes);

        String Ingredients=IngredientsWithEcodes+ IngredientsWithoutEcodes;




        String statusString=null;
        String statusDetails=null;
        String allergyDetails=null;
        if(status_food){
            statusString="HALAL";
            statusDetails=  "You can take this food \n100% halal";
        }else {
            statusString = "HARAM";
            statusDetails = "These ingredients are Haram: \n" + HaramIngredientsWithEcodes+ HaramIngredientsWithoutEcodes;
        }
        if(!Allergies.equals(""))
            allergyDetails="Don't use this product if you have these allergies\n"+Allergies;

        if(!check && !check2)
        {
            Ingredients=null;
            statusString=null;
            statusDetails="No Ecode are found";
        }

        TextView txtView = (TextView) findViewById(R.id.recog_text_view);
        txtView.setText(Ingredients);

        TextView tView = (TextView) findViewById(R.id.status_text);
        tView.setText(statusString);

        TextView tdView = (TextView) findViewById(R.id.status_details);
        tdView.setText(statusDetails);

        TextView tAView = (TextView) findViewById(R.id.allergies_details);
        tAView.setText(allergyDetails);


    }





    //returns the allergies that can be present in food
    private String getAllergies(String message) {
        StringBuilder allergies = new StringBuilder();

        String[] stringOfWords = new String[0];

        stringOfWords = message.split("\\W+");

        for (int i = 0; i < stringOfWords.length; i++) {
                for(int j=0;j<allergyLength;j++){
                    if(stringOfWords[i].toLowerCase().equals(allergy.get(j).getName().toLowerCase())){
                        if(!allergies.toString().contains(stringOfWords[i]))
                                allergies.append(stringOfWords[i]+"  -  "+allergy.get(j).getValue()+ " \n");
                    }
                }

            }


        String ecodeString = allergies.toString();
        return ecodeString;
    }


    //save haram ingredients name to string  HaramIngredientsWithEcodes
    private void haramIngFinder(String ecodes) {
        String [] ecodesSplit=new String[0];
        StringBuilder haramIng= new StringBuilder();
        ecodesSplit = ecodes.split("\\W+");
        for (int i=0 ; i<ecodesSplit.length;i++){
            for(int j=0;j<dataLength;j++){
                if(ecodesSplit[i].equals(data.get(j).getEcode())){
                    if(data.get(j).getValue().equals("HARAM"))
                    {
                        haramIng.append(ecodesSplit[i] + "  --  " + data.get(j).getName() + "\n");
                    }
                }
            }

        }
        HaramIngredientsWithEcodes= haramIng.toString();
    }

    //
    private void getname(String ecodes) {
        String[] ecodesSplit = new String[0];
        StringBuilder withName = new StringBuilder();
        ecodesSplit = ecodes.split("\\W+");

        for (int i = 0; i < ecodesSplit.length ; i++) {
            for (int j = 0; j < dataLength; j++) {
                if (ecodesSplit[i].equals(data.get(j).getEcode())) {
                    withName.append(ecodesSplit[i] + "  -  " + data.get(j).getName() + "\n");
                }
            }
        }
        IngredientsWithEcodes=withName.toString();
    }

    //check food status based on ecode of ingredients
    private Boolean checkFood(String ecodes) {
        Boolean status= true;
        String [] ecodesSplit=new String[0];
        ecodesSplit = ecodes.split("\\W+");

        for (int i=0 ; i<ecodesSplit.length;i++){
            for(int j=0;j<dataLength;j++){
                if(ecodesSplit[i].equals(data.get(j).getEcode())){
                    if(data.get(j).getValue().equals("HARAM"))
                    {
                        status=false;
                    }
                    if(!status)
                        break;
                }
            }
        }

        return status;
    }

    //check food status based on name of ingredients
    private boolean checkFoodData(String message) {
        StringBuilder Ingredients = new StringBuilder();
        StringBuilder HaramIngredients = new StringBuilder();
        boolean status = true;



        for(int j=0;j<foodDataLength;j++){
            if(message.toLowerCase().contains(foodData.get(j).getName().toLowerCase()))
            {
                check2=true;
                if(!IngredientsWithEcodes.contains(foodData.get(j).getName()))
                        Ingredients.append(foodData.get(j).getName()+" \n");

                if(foodData.get(j).getValue().equals("Haram"))
                {
                    HaramIngredients.append(foodData.get(j).getName()+" \n");
                    status = false;
                }

            }
        }

        IngredientsWithoutEcodes=Ingredients.toString();
        HaramIngredientsWithoutEcodes=HaramIngredients.toString();
        return status;
    }



    //returns the ecodes
    private String giveEcodes(String Message){
        StringBuilder ecodes=new StringBuilder();

        String [] stringOfWords = new String[0];

        stringOfWords = Message.split("\\W+");

        int j=0;
        for (int i = 0; i < stringOfWords.length; i++) {

            if((stringOfWords[i].startsWith("E") || stringOfWords[i].startsWith("e") )
                    &&(stringOfWords[i].endsWith("0")||stringOfWords[i].endsWith("1")
                    ||stringOfWords[i].endsWith("2")||stringOfWords[i].endsWith("3")
                    ||stringOfWords[i].endsWith("4")||stringOfWords[i].endsWith("5")
                    ||stringOfWords[i].endsWith("6")||stringOfWords[i].endsWith("7")
                    ||stringOfWords[i].endsWith("8")||stringOfWords[i].endsWith("9")
                    ||stringOfWords[i].endsWith("a")||stringOfWords[i].endsWith("b")
                    ||stringOfWords[i].endsWith("c")||stringOfWords[i].endsWith("d")
                    ||stringOfWords[i].endsWith("e")||stringOfWords[i].endsWith("f")
                    ||stringOfWords[i].endsWith("i")||stringOfWords[i].endsWith("ii"))){
                check=true;
                ecodes.append(stringOfWords[i]+" \n");
               // Log.v("ReCOD","ecode : " +stringOfWords[i]);
            }
        }

        String ecodeString = ecodes.toString();
        return ecodeString;
    }



    private void readEcodes() {
        InputStream IS= getResources().openRawResource(R.raw.data);
        BufferedReader reader= new BufferedReader(
                new InputStreamReader(IS, Charset.forName("UTF-8"))
        );

        String line = " ";
        try{
            reader.readLine();
            while((line = reader.readLine()) != null){
                // split by ,
                String[] tokens= line.split(",");

                //read the data
                EcodeSample sample = new EcodeSample();
                sample.setEcode(tokens[0]);
                sample.setValue(tokens[1]);
                sample.setName(tokens[2]);
                data.add(sample);

                dataLength++;

            }
        } catch (IOException e){
            Log.wtf("Recog activity","Error reading data file on line "+ line ,e);
            e.printStackTrace();
        }

    }


    private void readAllergy() {
        InputStream IS= getResources().openRawResource(R.raw.allergy);
        BufferedReader reader= new BufferedReader(
                new InputStreamReader(IS, Charset.forName("UTF-8"))
        );
        String line = " ";
        try{
            reader.readLine();
            while((line = reader.readLine()) != null){
                //split by ,
                String[] tokens= line.split(",");

                //read the data
                Food  sample = new Food();
                sample.setName(tokens[0]);
                sample.setValue(tokens[1]);
                allergy.add(sample);

                allergyLength++;

            }
        } catch (IOException e){
            Log.wtf("Recog activity","Error reading data file on line "+ line ,e);
            e.printStackTrace();
        }
    }

    private void readFood() {
        InputStream IS= getResources().openRawResource(R.raw.foodbook);
        BufferedReader reader= new BufferedReader(
                new InputStreamReader(IS, Charset.forName("UTF-8"))
        );
        String line = " ";
        try{
            while((line = reader.readLine()) != null){
                // split by ,
                String[] tokens= line.split(",");

                //read the data
                Food sample = new Food();
                sample.setName(tokens[0]);
                sample.setValue(tokens[1]);
                foodData.add(sample);

                foodDataLength++;
                Log.v("Reading Food","data: "+sample.toString()+" len: "+foodDataLength);

            }
        } catch (IOException e){
            Log.wtf("Recog activity","Error reading data file on line "+ line ,e);
            e.printStackTrace();
        }
    }
}