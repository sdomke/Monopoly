package com.monopoly.domke.sebastian.monopoly.view;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.monopoly.domke.sebastian.monopoly.R;
import com.monopoly.domke.sebastian.monopoly.common.GameConnection;
import com.monopoly.domke.sebastian.monopoly.common.GameMessage;
import com.monopoly.domke.sebastian.monopoly.common.Spiel;
import com.monopoly.domke.sebastian.monopoly.common.Spieler;
import com.monopoly.domke.sebastian.monopoly.database.DatabaseHandler;
import com.monopoly.domke.sebastian.monopoly.helper.HostMessageInterpreter;
import com.monopoly.domke.sebastian.monopoly.helper.MessageParser;
import com.monopoly.domke.sebastian.monopoly.helper.NsdClient;
import com.monopoly.domke.sebastian.monopoly.helper.NsdServer;
import com.monopoly.domke.sebastian.monopoly.helper.PlayerMessageInterpreter;

import org.json.JSONObject;

import java.util.ArrayList;

public class SpielStartActivity extends AppCompatActivity {

    ArrayList<String> aktuelleSpielerIMEIs;
    ArrayList<Spieler> aktuelleSpieler;
    int aktuellesSpielID;
    public Spiel aktuellesSpiel;
    public Spieler eigenerSpieler;
    TextView aktuellesKapitalEigenerSpielerTextView;
    EditText aktuellerBetragEditText;
    int hypothek = 0;
    int empfaengerAuswahl = 0;

    public DatabaseHandler databaseHandler;

    public NsdClient mNsdClient;
    public GameConnection mGameConnection;
    private Handler mUpdateHandler;
    public static final String TAG = "NsdGame";

    boolean neuesSpiel = false;

    private PlayerMessageInterpreter playerMessageInterpreter;
    private HostMessageInterpreter hostMessageInterpreter;
    public MessageParser messageParser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //ToDo Fragment mit dem aktuellen Kapital der Gegenspieler und Wert von Frei Parken implementieren

        //ToDo Buttons für Erhalten und Bezahlen als Fragment implementieren

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spiel_start);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        databaseHandler = new DatabaseHandler(this);

        messageParser = new MessageParser();

        Intent intent = getIntent();

        aktuelleSpielerIMEIs = intent.getStringArrayListExtra("aktive_spieler");
        aktuellesSpielID = intent.getIntExtra("aktuellesSpielID", 0);
        neuesSpiel = intent.getBooleanExtra("neues_spiel", false);

        aktuellesSpiel = databaseHandler.getSpielByID(aktuellesSpielID);

        gegenspielerInit();

        mUpdateHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                Toast.makeText(getApplicationContext(), msg.getData().getString("msg"), Toast.LENGTH_SHORT).show();

                try {
                    GameMessage message = messageParser.jsonStringToMessage(msg.getData().getString("msg"));

                    if (!neuesSpiel) {
                        Toast.makeText(getApplicationContext(), "Client Message Handler", Toast.LENGTH_SHORT).show();
                        playerMessageInterpreter.decideWhatToDoWithTheMassage(message);
                    } else {
                        Toast.makeText(getApplicationContext(), "Host Message Handler", Toast.LENGTH_SHORT).show();
                        hostMessageInterpreter.decideWhatToDoWithTheMassage(message);
                    }
                }catch (Exception e){
                    Log.d(TAG, "Keine passende Nachricht");
                    Toast.makeText(getApplicationContext(), "Keine passende Nachricht", Toast.LENGTH_SHORT).show();
                }
            }
        };

        mGameConnection = new GameConnection(mUpdateHandler);

        /*mNsdServer = new NsdServer(this);
        //mNsdServer.initializeNsd();
        mNsdServer.initializeRegistrationListener();*/



            mNsdClient = new NsdClient(this, mGameConnection);
            mNsdClient.initializeDiscoveryListener();
            mNsdClient.initializeResolveListener();

        if(!neuesSpiel) {
            playerMessageInterpreter = new PlayerMessageInterpreter(this);
        }
        else{
            hostMessageInterpreter = new HostMessageInterpreter(this);
        }

        init();
    }

    //Todo EigenenSpieler und Gegenspieler Touchlistener initialisieren
    public void init(){
        aktuellesKapitalEigenerSpielerTextView = (TextView) findViewById(R.id.deinKapitalTextView);
        try {
            aktuellesKapitalEigenerSpielerTextView.setText(String.valueOf(eigenerSpieler.getSpielerKapital()));
        }
        catch(Exception e){
            aktuellesKapitalEigenerSpielerTextView.setText("0");
        }

        final ImageView eigenerSpielerIconImageView = (ImageView) findViewById(R.id.eigenerSpielerFarbeButtonView);
        ImageView gegenspieler1IconImageView = (ImageView) findViewById(R.id.spielerFarbeRotButtonView);
        ImageView gegenspieler2IconImageView = (ImageView) findViewById(R.id.spielerFarbeBlauButtonView);
        ImageView gegenspieler3IconImageView = (ImageView) findViewById(R.id.spielerFarbeGruenButtonView);
        TextView bankIconImageView = (TextView) findViewById(R.id.bankButtonView);
        TextView mitteIconImageView = (TextView) findViewById(R.id.mitteButtonView);

        aktuellerBetragEditText = (EditText) findViewById(R.id.aktuellerBetragEditTextView);

        TextView plus4MButtonView = (TextView) findViewById(R.id.vierMillionenDollar);
        TextView plus2MButtonView = (TextView) findViewById(R.id.zweiMillionenDollar);
        TextView plus1MButtonView = (TextView) findViewById(R.id.eineMillionenDollar);
        TextView plus500KButtonView = (TextView) findViewById(R.id.fuenfhundertTausendDollar);
        TextView plus400KButtonView = (TextView) findViewById(R.id.vierhundertTausendDollar);
        TextView plus200KButtonView = (TextView) findViewById(R.id.zweihundertTausendDollar);
        TextView plus100KButtonView = (TextView) findViewById(R.id.hundertTausendDollar);
        TextView plus50KButtonView = (TextView) findViewById(R.id.fuenfzigTausendDollar);
        TextView plus10KButtonView = (TextView) findViewById(R.id.zehnTausendDollar);
        TextView plus5KButtonView = (TextView) findViewById(R.id.fuenfTausendDollar);

        TextView erhalteLosButtonView = (TextView) findViewById(R.id.erhalteLos);
        TextView erhalteFreiParkenButtonView = (TextView) findViewById(R.id.erhalteFreiParken);
        TextView erhalteHypothekButtonView = (TextView) findViewById(R.id.erhalteHypothek);

        TextView bezahleGefaengnisButtonView = (TextView) findViewById(R.id.zahleGefaengnis);
        TextView bezahleHypothekButtonView = (TextView) findViewById(R.id.zahleHypothek);

        ImageView transaktionButtonView = (ImageView) findViewById(R.id.transaktionButton);

        transaktionButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                transaktionDurchfuehren();
            }
        });

        eigenerSpielerIconImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                empfaengerAuswahl = 1;
                Toast.makeText(getApplicationContext(), "Eigener Spieler ausgewählt", Toast.LENGTH_SHORT).show();
            }
        });

        gegenspieler1IconImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                empfaengerAuswahl = 4;
                Toast.makeText(getApplicationContext(), "Spieler 1 ausgewählt", Toast.LENGTH_SHORT).show();
            }
        });

        gegenspieler2IconImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                empfaengerAuswahl = 5;
                Toast.makeText(getApplicationContext(), "Spieler 2 ausgewählt", Toast.LENGTH_SHORT).show();
            }
        });

        gegenspieler3IconImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                empfaengerAuswahl = 6;
                Toast.makeText(getApplicationContext(), "Spieler 3 ausgewählt", Toast.LENGTH_SHORT).show();
            }
        });

        bankIconImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                empfaengerAuswahl = 2;
                Toast.makeText(getApplicationContext(), "Bank ausgewählt", Toast.LENGTH_SHORT).show();
            }
        });

        mitteIconImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                empfaengerAuswahl = 3;
                Toast.makeText(getApplicationContext(), "Mitte ausgewählt", Toast.LENGTH_SHORT).show();
            }
        });

        plus4MButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                betragHinzu(4000000);
            }
        });

        plus2MButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                betragHinzu(2000000);
            }
        });

        plus1MButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                betragHinzu(1000000);
            }
        });

        plus500KButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                betragHinzu(500000);
            }
        });

        plus400KButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                betragHinzu(400000);
            }
        });

        plus200KButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                betragHinzu(200000);
            }
        });

        plus100KButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                betragHinzu(100000);
            }
        });

        plus50KButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                betragHinzu(50000);
            }
        });

        plus10KButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                betragHinzu(10000);
            }
        });

        plus5KButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                betragHinzu(5000);
            }
        });

        erhalteLosButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int betrag = 2000000;

                eigenerSpieler.setSpielerKapital(betrag + eigenerSpieler.getSpielerKapital());
                aktuellesKapitalEigenerSpielerTextView.setText(String.valueOf(eigenerSpieler.getSpielerKapital()));

                JSONObject messageContent = messageParser.moneyTransactionToJson(eigenerSpieler, betrag);

                GameMessage receiveLosGameMessage = new GameMessage(GameMessage.MessageHeader.receiveMoneyFromBank, messageContent);

                String jsonString = messageParser.messageToJsonString(receiveLosGameMessage);

                mGameConnection.sendMessage(jsonString);
            }
        });

        erhalteFreiParkenButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int betrag = aktuellesSpiel.getFreiParken();

                eigenerSpieler.setSpielerKapital(eigenerSpieler.getSpielerKapital() - betrag);
                aktuellesKapitalEigenerSpielerTextView.setText(String.valueOf(eigenerSpieler.getSpielerKapital()));
                aktuellesSpiel.setFreiParken(0);

                JSONObject messageContent = messageParser.moneyTransactionToJson(eigenerSpieler, betrag);

                GameMessage receiveFreiParkenGameMessage = new GameMessage(GameMessage.MessageHeader.receiveFreiParken, messageContent);

                String jsonString = messageParser.messageToJsonString(receiveFreiParkenGameMessage);

                mGameConnection.sendMessage(jsonString);
            }
        });

        erhalteHypothekButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(aktuellerBetragEditText.getText().toString().length() != 0) {

                    Toast.makeText(getApplicationContext(), "Funktion ist noch nicht fertig :-)", Toast.LENGTH_SHORT).show();

                    /*eigenerSpieler.setSpielerKapital(Integer.valueOf(aktuellerBetragEditText.getText().toString()) + eigenerSpieler.getSpielerKapital());
                    aktuellesKapitalEigenerSpielerTextView.setText(String.valueOf(eigenerSpieler.getSpielerKapital()));

                    hypothek += Integer.valueOf(aktuellerBetragEditText.getText().toString());
                    aktuellerBetragEditText.setText("");*/
                }
                else{
                    Toast.makeText(getApplicationContext(), "Bitte zuerst gewünschten Betrag für die Hypothek eingeben", Toast.LENGTH_SHORT).show();
                }
            }
        });

        bezahleGefaengnisButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int betrag = 500000;

                eigenerSpieler.setSpielerKapital(eigenerSpieler.getSpielerKapital() - betrag);
                aktuellesKapitalEigenerSpielerTextView.setText(String.valueOf(eigenerSpieler.getSpielerKapital()));

                aktuellesSpiel.setFreiParken(aktuellesSpiel.getFreiParken() + betrag);

                JSONObject messageContent = messageParser.moneyTransactionToJson(eigenerSpieler, betrag);

                GameMessage moneyTransactionToBankGameMessage = new GameMessage(GameMessage.MessageHeader.sendMoneyToFreiParken, messageContent);

                String jsonString = messageParser.messageToJsonString(moneyTransactionToBankGameMessage);

                mGameConnection.sendMessage(jsonString);
            }
        });

        bezahleHypothekButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Todo Hypothek darf nicht negativ werden (nur so viel bezahlen so hoch wie die Hypothek)
                if(aktuellerBetragEditText.getText().toString().length() != 0) {

                    Toast.makeText(getApplicationContext(), "Funktion ist noch nicht fertig :-)", Toast.LENGTH_SHORT).show();

/*                  eigenerSpieler.setSpielerKapital(eigenerSpieler.getSpielerKapital() - Integer.valueOf(aktuellerBetragEditText.getText().toString()));
                    aktuellesKapitalEigenerSpielerTextView.setText(String.valueOf(eigenerSpieler.getSpielerKapital()));

                    hypothek -= Integer.valueOf(aktuellerBetragEditText.getText().toString());
                    aktuellerBetragEditText.setText("");*/
                }
                else{
                    Toast.makeText(getApplicationContext(), "Bitte zuerst gewünschten Betrag für die Hypothek eingeben", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void transaktionDurchfuehren(){

        if(aktuellerBetragEditText.getText().toString().length() != 0) {

            transaktionEmpfaenger();
        }
        else{
            Toast.makeText(getApplicationContext(), "Bitte zuerst gewünschten Betrag für die Überweisung eingeben", Toast.LENGTH_SHORT).show();
        }
    }

    public void betragHinzu(int betrag){

        if(aktuellerBetragEditText.getText().toString().isEmpty()){
            aktuellerBetragEditText.setText(String.valueOf(betrag));
        }
        else{
            aktuellerBetragEditText.setText(String.valueOf(Integer.valueOf(aktuellerBetragEditText.getText().toString()) + betrag));
        }
    }

    public void transaktionEmpfaenger(){

        switch (empfaengerAuswahl){
            case 0: Toast.makeText(getApplicationContext(), "Bitte zuerst einen Empfänger für die Überweisung auswählen", Toast.LENGTH_SHORT).show();
                break;
            case 1: transaktionAnMich();
                break;
            case 2: transaktionAnDieBank();
                break;
            case 3:  transaktionInDieMitte();
                break;
            case 4: case 5: case 6: case 7: case 8: case 9: case 10: transaktionAnEinenSpieler();
                break;

        }
    }

    public void transaktionAnMich() {

        int betrag = Integer.valueOf(aktuellerBetragEditText.getText().toString());

        eigenerSpieler.setSpielerKapital(betrag + eigenerSpieler.getSpielerKapital());
        aktuellesKapitalEigenerSpielerTextView.setText(String.valueOf(eigenerSpieler.getSpielerKapital()));

        JSONObject messageContent = messageParser.moneyTransactionToJson(eigenerSpieler, betrag);

        GameMessage moneyTransactionToPlayerGameMessage = new GameMessage(GameMessage.MessageHeader.receiveMoneyFromBank, messageContent);

        String jsonString = messageParser.messageToJsonString(moneyTransactionToPlayerGameMessage);

        mGameConnection.sendMessage(jsonString);

        aktuellerBetragEditText.setText("");
        empfaengerAuswahl = 0;


    }

    public void transaktionAnDieBank() {

        int betrag = Integer.valueOf(aktuellerBetragEditText.getText().toString());

        eigenerSpieler.setSpielerKapital(eigenerSpieler.getSpielerKapital() - betrag);
        aktuellesKapitalEigenerSpielerTextView.setText(String.valueOf(eigenerSpieler.getSpielerKapital()));

        JSONObject messageContent = messageParser.moneyTransactionToJson(eigenerSpieler, betrag);

        GameMessage moneyTransactionToBankGameMessage = new GameMessage(GameMessage.MessageHeader.sendMoneyToBank, messageContent);

        String jsonString = messageParser.messageToJsonString(moneyTransactionToBankGameMessage);

        mGameConnection.sendMessage(jsonString);

        aktuellerBetragEditText.setText("");
        empfaengerAuswahl = 0;
    }

    public void transaktionInDieMitte() {

        int betrag = Integer.valueOf(aktuellerBetragEditText.getText().toString());

        eigenerSpieler.setSpielerKapital(eigenerSpieler.getSpielerKapital() - betrag);
        aktuellesKapitalEigenerSpielerTextView.setText(String.valueOf(eigenerSpieler.getSpielerKapital()));

        aktuellesSpiel.setFreiParken(aktuellesSpiel.getFreiParken() + betrag);

        JSONObject messageContent = messageParser.moneyTransactionToJson(eigenerSpieler, betrag);

        GameMessage moneyTransactionToBankGameMessage = new GameMessage(GameMessage.MessageHeader.sendMoneyToFreiParken, messageContent);

        String jsonString = messageParser.messageToJsonString(moneyTransactionToBankGameMessage);

        mGameConnection.sendMessage(jsonString);

        aktuellerBetragEditText.setText("");
        empfaengerAuswahl = 0;
    }

    public void transaktionAnEinenSpieler() {

        int betrag = Integer.valueOf(aktuellerBetragEditText.getText().toString());

        eigenerSpieler.setSpielerKapital(eigenerSpieler.getSpielerKapital() - betrag);
        aktuellesKapitalEigenerSpielerTextView.setText(String.valueOf(eigenerSpieler.getSpielerKapital()));

        //Todo Überweisung an den ausgewählten Spieler

        JSONObject messageContent = messageParser.moneyTransactionToPlayerToJson(eigenerSpieler, eigenerSpieler, betrag);

        GameMessage moneyTransactionToBankGameMessage = new GameMessage(GameMessage.MessageHeader.sendMoney, messageContent);

        String jsonString = messageParser.messageToJsonString(moneyTransactionToBankGameMessage);

        mGameConnection.sendMessage(jsonString);

        aktuellerBetragEditText.setText("");
        empfaengerAuswahl = 0;
    }

    public void gegenspielerInit(){

        String imeiEigenerSpieler = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        aktuelleSpieler = new ArrayList<>();

        for (String aktuelleSpielerIMEI: aktuelleSpielerIMEIs) {
            Spieler spieler = databaseHandler.getSpielerBySpielIdAndSpielerIMEI(aktuellesSpiel.getSpielID(), aktuelleSpielerIMEI);

            aktuelleSpieler.add(spieler);

            if(spieler.getSpielerIMEI().equals(imeiEigenerSpieler)){
                eigenerSpieler = spieler;
            }
        }
    }

    public void spielBeenden(){

        new AlertDialog.Builder(this)
                .setTitle("Spiel beenden")
                .setMessage("Möchtest du das Spiel wirklich beenden?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent intent = new Intent(getApplicationContext(), MainMenuActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        JSONObject messageContent = messageParser.gameStatusToJson(eigenerSpieler, aktuellesSpiel);

                        GameMessage startGameMessage = new GameMessage(GameMessage.MessageHeader.gameStart, messageContent);

                        String jsonString = messageParser.messageToJsonString(startGameMessage);

                        mGameConnection.sendMessage(jsonString);

                        Toast.makeText(getApplicationContext(), "Spiel beenden Nachricht gesendet", Toast.LENGTH_SHORT).show();

                        startActivity(intent);
                    }
                }).create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate themenu; this adds items to the action bar if it is present.

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.spiel_start_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.spiel_beenden_action) {

            spielBeenden();
        }
        return true;
    }
}
