package com.monopoly.domke.sebastian.monopoly.view;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.monopoly.domke.sebastian.monopoly.R;
import com.monopoly.domke.sebastian.monopoly.common.GameConnectionService;
import com.monopoly.domke.sebastian.monopoly.common.GameMessage;
import com.monopoly.domke.sebastian.monopoly.common.Spiel;
import com.monopoly.domke.sebastian.monopoly.common.Spieler;
import com.monopoly.domke.sebastian.monopoly.database.DatabaseHandler;
import com.monopoly.domke.sebastian.monopoly.helper.GameStatusAdapter;
import com.monopoly.domke.sebastian.monopoly.helper.HostMessageInterpreter;
import com.monopoly.domke.sebastian.monopoly.helper.MessageParser;
import com.monopoly.domke.sebastian.monopoly.helper.NsdHelper;
import com.monopoly.domke.sebastian.monopoly.helper.PlayerMessageInterpreter;

import org.json.JSONObject;

import java.util.ArrayList;

public class SpielStartActivity extends AppCompatActivity implements GameStatusFragment.OnFragmentInteractionListener {

    public boolean mServiceBound = false;
    public GameConnectionService mGameConnectionService;

    public ListView gameStatusListView;
    public GameStatusAdapter game_status_adapter;

    ArrayList<String> aktuelleSpielerIMEIs;
    public ArrayList<Spieler> gegenspielerListe;

    public ArrayList<Spieler> gameStatusListe;
    int aktuellesSpielID;
    public Spiel aktuellesSpiel;
    public Spieler eigenerSpieler;
    Spieler mitteSpieler;
    public TextView aktuellesKapitalEigenerSpielerTextView;
    EditText aktuellerBetragEditText;
    double hypothek = 0;
    int empfaengerAuswahl = 0;

    ImageView gameStatusButton;

    public DatabaseHandler databaseHandler;

    public static final String TAG = "NsdGame";

    boolean neuesSpiel = false;

    private PlayerMessageInterpreter playerMessageInterpreter;
    private HostMessageInterpreter hostMessageInterpreter;
    public MessageParser messageParser;

    private static final String BROADCAST_INTENT = "BROADCAST_INTENT";
    private static final String BROADCAST_INTENT_EXTRA = "BROADCAST_INTENT_EXTRA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //ToDo Buttons für Erhalten und Bezahlen als Fragment implementieren

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spiel_start);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        databaseHandler = new DatabaseHandler(this);

        //JobManager.create(this).addJobCreator(new GameJobCreator());

        messageParser = new MessageParser();

        Intent intent = getIntent();

        aktuelleSpielerIMEIs = intent.getStringArrayListExtra("aktive_spieler");
        aktuellesSpielID = intent.getIntExtra("aktuellesSpielID", 0);
        neuesSpiel = intent.getBooleanExtra("neues_spiel", false);

        aktuellesSpiel = databaseHandler.getSpielByID(aktuellesSpielID);

        spielerInit();

        eigenerSpielerButtonViewInit();
        gegenspielerButtonViewsInit();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BROADCAST_INTENT);
        LocalBroadcastManager.getInstance(SpielStartActivity.this).registerReceiver(messageReceiver, filter);

        if(!neuesSpiel) {
            Intent gameConnectionServiceIntent = new Intent(getApplicationContext(), GameConnectionService.class);
            getApplicationContext().bindService(gameConnectionServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
            playerMessageInterpreter = new PlayerMessageInterpreter(this);
        }
        else{
            Intent gameConnectionServiceIntent = new Intent(getApplicationContext(), GameConnectionService.class);
            getApplicationContext().bindService(gameConnectionServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
            hostMessageInterpreter = new HostMessageInterpreter(this);
            //advertiseGame();
        }

        init();
    }

    public BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BROADCAST_INTENT:
                    String msg = intent.getStringExtra(BROADCAST_INTENT_EXTRA);
                    try {
                        GameMessage message = messageParser.jsonStringToMessage(msg);

                        //Toast.makeText(getApplicationContext(), "Client Message Handler", Toast.LENGTH_SHORT).show();
                        if (!neuesSpiel) {
                            //Toast.makeText(getApplicationContext(), "Client Message Handler", Toast.LENGTH_SHORT).show();
                            playerMessageInterpreter.decideWhatToDoWithTheMassage(message);
                        } else {
                            //Toast.makeText(getApplicationContext(), "Host Message Handler", Toast.LENGTH_SHORT).show();
                            hostMessageInterpreter.decideWhatToDoWithTheMassage(message);
                        }

                    }catch (Exception e){
                        Log.d(TAG, "Keine passende Nachricht");
                        //Toast.makeText(getApplicationContext(), "Keine passende Nachricht", Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public void init(){
        gameStatusListView = (ListView) findViewById(R.id.gameStatusListView);

        gameStatusListe = gegenspielerListe;

        mitteSpieler = new Spieler("MitteSpieler", aktuellesSpiel.getSpielID());

        mitteSpieler.setSpielerName("Mitte");
        mitteSpieler.setSpielerFarbe(R.color.mitte_farbe);
        mitteSpieler.setSpielerKapital(aktuellesSpiel.getFreiParken());

        gameStatusListe.add(mitteSpieler);

        game_status_adapter = new GameStatusAdapter(this,
                R.layout.list_item_spieluebersicht, gegenspielerListe);
        gameStatusListView.setAdapter(game_status_adapter);

        gameStatusListView.setVisibility(View.INVISIBLE);

        gameStatusButton = (ImageView) findViewById(R.id.gameStatusButtonView);

        gameStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(gameStatusListView.isShown()) {
                    gameStatusListView.setVisibility(View.INVISIBLE);
                }
                else{
                    mitteSpieler.setSpielerKapital(aktuellesSpiel.getFreiParken());
                    gameStatusListe.set(gameStatusListe.size() - 1, mitteSpieler);
                    game_status_adapter.notifyDataSetChanged();
                    gameStatusListView.setVisibility(View.VISIBLE);
                }


            }
        });

        aktuellesKapitalEigenerSpielerTextView = (TextView) findViewById(R.id.deinKapitalTextView);
        try {
            aktuellesKapitalEigenerSpielerTextView.setText(String.valueOf(eigenerSpieler.getSpielerKapital()));
        }
        catch(Exception e){
            aktuellesKapitalEigenerSpielerTextView.setText("0");
        }

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
        ImageView deleteButtonView = (ImageView) findViewById(R.id.deleteButton);

        transaktionButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                transaktionDurchfuehren();
            }
        });

        deleteButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                aktuellerBetragEditText.setText("");
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
                betragHinzu(4);
            }
        });

        plus2MButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                betragHinzu(2);
            }
        });

        plus1MButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                betragHinzu(1);
            }
        });

        plus500KButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                betragHinzu(0.5);
            }
        });

        plus400KButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                betragHinzu(0.4);
            }
        });

        plus200KButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                betragHinzu(0.2);
            }
        });

        plus100KButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                betragHinzu(0.1);
            }
        });

        plus50KButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                betragHinzu(0.05);
            }
        });

        plus10KButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                betragHinzu(0.01);
            }
        });

        plus5KButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                betragHinzu(0.005);
            }
        });

        erhalteLosButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double betrag = 2;

                eigenerSpieler.setSpielerKapital(roundDouble(betrag + eigenerSpieler.getSpielerKapital()));
                aktuellesKapitalEigenerSpielerTextView.setText(String.valueOf(eigenerSpieler.getSpielerKapital()));

                databaseHandler.updateSpieler(eigenerSpieler);

                JSONObject messageContent = messageParser.moneyTransactionToJson(eigenerSpieler, betrag);

                GameMessage receiveLosGameMessage = new GameMessage(GameMessage.MessageHeader.receiveMoneyFromBank, messageContent);

                String jsonString = messageParser.messageToJsonString(receiveLosGameMessage);

                mGameConnectionService.mGameConnection.sendMessageToAllClients(jsonString);

                Toast.makeText(getApplicationContext(), "Du hast " + betrag + " M$ für Los erhalten!", Toast.LENGTH_SHORT).show();

            }
        });

        erhalteFreiParkenButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                double betrag = aktuellesSpiel.getFreiParken();

                eigenerSpieler.setSpielerKapital(roundDouble(eigenerSpieler.getSpielerKapital() + betrag));
                aktuellesKapitalEigenerSpielerTextView.setText(String.valueOf(eigenerSpieler.getSpielerKapital()));
                aktuellesSpiel.setFreiParken(0);

                databaseHandler.updateSpieler(eigenerSpieler);
                databaseHandler.updateSpiel(aktuellesSpiel);

                JSONObject messageContent = messageParser.moneyTransactionToJson(eigenerSpieler, betrag);

                GameMessage receiveFreiParkenGameMessage = new GameMessage(GameMessage.MessageHeader.receiveFreiParken, messageContent);

                String jsonString = messageParser.messageToJsonString(receiveFreiParkenGameMessage);

                mGameConnectionService.mGameConnection.sendMessageToAllClients(jsonString);

                Toast.makeText(getApplicationContext(), "Du hast " + betrag + " M$ von Frei Parken erhalten!", Toast.LENGTH_SHORT).show();

            }
        });

        erhalteHypothekButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(aktuellerBetragEditText.getText().toString().length() != 0) {

                    Toast.makeText(getApplicationContext(), "Funktion ist noch nicht fertig :-)", Toast.LENGTH_SHORT).show();

                    /*eigenerSpieler.setSpielerKapital(Double.valueOf(aktuellerBetragEditText.getText().toString()) + eigenerSpieler.getSpielerKapital());
                    aktuellesKapitalEigenerSpielerTextView.setText(String.valueOf(eigenerSpieler.getSpielerKapital()));

                    hypothek += Double.valueOf(aktuellerBetragEditText.getText().toString());
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
                double betrag = 0.5;

                eigenerSpieler.setSpielerKapital(roundDouble(eigenerSpieler.getSpielerKapital() - betrag));
                aktuellesKapitalEigenerSpielerTextView.setText(String.valueOf(eigenerSpieler.getSpielerKapital()));

                aktuellesSpiel.setFreiParken(roundDouble(aktuellesSpiel.getFreiParken() + betrag));

                databaseHandler.updateSpieler(eigenerSpieler);
                databaseHandler.updateSpiel(aktuellesSpiel);

                JSONObject messageContent = messageParser.moneyTransactionToJson(eigenerSpieler, betrag);

                GameMessage moneyTransactionToBankGameMessage = new GameMessage(GameMessage.MessageHeader.sendMoneyToFreiParken, messageContent);

                String jsonString = messageParser.messageToJsonString(moneyTransactionToBankGameMessage);

                mGameConnectionService.mGameConnection.sendMessageToAllClients(jsonString);

                Toast.makeText(getApplicationContext(), "Du hast " + betrag + " M$ in die Mitte gezahlt!", Toast.LENGTH_SHORT).show();

            }
        });

        bezahleHypothekButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Todo Hypothek darf nicht negativ werden (nur so viel bezahlen so hoch wie die Hypothek)
                if(aktuellerBetragEditText.getText().toString().length() != 0) {

                    Toast.makeText(getApplicationContext(), "Funktion ist noch nicht fertig :-)", Toast.LENGTH_SHORT).show();

/*                  eigenerSpieler.setSpielerKapital(eigenerSpieler.getSpielerKapital() - Double.valueOf(aktuellerBetragEditText.getText().toString()));
                    aktuellesKapitalEigenerSpielerTextView.setText(String.valueOf(eigenerSpieler.getSpielerKapital()));

                    hypothek -= Double.valueOf(aktuellerBetragEditText.getText().toString());
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

    public void betragHinzu(double betrag){

        if(aktuellerBetragEditText.getText().toString().isEmpty()){
            aktuellerBetragEditText.setText(String.valueOf(betrag));
        }
        else{
            aktuellerBetragEditText.setText(String.valueOf(roundDouble(Double.valueOf(aktuellerBetragEditText.getText().toString()) + betrag)));
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

        double betrag = Double.valueOf(aktuellerBetragEditText.getText().toString());

        Log.d(TAG, "Betrag: " + betrag);

        eigenerSpieler.setSpielerKapital(roundDouble(betrag + eigenerSpieler.getSpielerKapital()));

        Log.d(TAG, "KapitalEigenerSpieler: " + eigenerSpieler.getSpielerKapital());

        aktuellesKapitalEigenerSpielerTextView.setText(String.valueOf(eigenerSpieler.getSpielerKapital()));

        databaseHandler.updateSpieler(eigenerSpieler);

        JSONObject messageContent = messageParser.moneyTransactionToJson(eigenerSpieler, betrag);

        GameMessage moneyTransactionToPlayerGameMessage = new GameMessage(GameMessage.MessageHeader.receiveMoneyFromBank, messageContent);

        String jsonString = messageParser.messageToJsonString(moneyTransactionToPlayerGameMessage);

        mGameConnectionService.mGameConnection.sendMessageToAllClients(jsonString);

        Toast.makeText(getApplicationContext(), "Du hast " + betrag + " M$ von der Bank erhalten!", Toast.LENGTH_SHORT).show();

        aktuellerBetragEditText.setText("");
        empfaengerAuswahl = 0;


    }

    public void transaktionAnDieBank() {

        double betrag = Double.valueOf(aktuellerBetragEditText.getText().toString());

        Log.d(TAG, "Betrag: " + betrag);

        eigenerSpieler.setSpielerKapital(roundDouble(eigenerSpieler.getSpielerKapital() - betrag));

        Log.d(TAG, "KapitalEigenerSpieler: " + eigenerSpieler.getSpielerKapital());

        aktuellesKapitalEigenerSpielerTextView.setText(String.valueOf(eigenerSpieler.getSpielerKapital()));

        databaseHandler.updateSpieler(eigenerSpieler);

        JSONObject messageContent = messageParser.moneyTransactionToJson(eigenerSpieler, betrag);

        GameMessage moneyTransactionToBankGameMessage = new GameMessage(GameMessage.MessageHeader.sendMoneyToBank, messageContent);

        String jsonString = messageParser.messageToJsonString(moneyTransactionToBankGameMessage);

        mGameConnectionService.mGameConnection.sendMessageToAllClients(jsonString);

        Toast.makeText(getApplicationContext(), "Du hast " + betrag + " M$ an die Bank überwiesen!", Toast.LENGTH_SHORT).show();

        aktuellerBetragEditText.setText("");
        empfaengerAuswahl = 0;
    }

    public void transaktionInDieMitte() {

        double betrag = Double.valueOf(aktuellerBetragEditText.getText().toString());

        eigenerSpieler.setSpielerKapital(roundDouble(eigenerSpieler.getSpielerKapital() - betrag));
        aktuellesKapitalEigenerSpielerTextView.setText(String.valueOf(eigenerSpieler.getSpielerKapital()));

        aktuellesSpiel.setFreiParken(roundDouble(aktuellesSpiel.getFreiParken() + betrag));

        databaseHandler.updateSpieler(eigenerSpieler);
        databaseHandler.updateSpiel(aktuellesSpiel);

        JSONObject messageContent = messageParser.moneyTransactionToJson(eigenerSpieler, betrag);

        GameMessage moneyTransactionToBankGameMessage = new GameMessage(GameMessage.MessageHeader.sendMoneyToFreiParken, messageContent);

        String jsonString = messageParser.messageToJsonString(moneyTransactionToBankGameMessage);

        mGameConnectionService.mGameConnection.sendMessageToAllClients(jsonString);

        Toast.makeText(getApplicationContext(), "Du hast " + betrag + " M$ in die Mitte gezahlt!", Toast.LENGTH_SHORT).show();

        aktuellerBetragEditText.setText("");
        empfaengerAuswahl = 0;
    }

    public void transaktionAnEinenSpieler() {

        double betrag = Double.valueOf(aktuellerBetragEditText.getText().toString());

        eigenerSpieler.setSpielerKapital(roundDouble(eigenerSpieler.getSpielerKapital() - betrag));
        aktuellesKapitalEigenerSpielerTextView.setText(String.valueOf(eigenerSpieler.getSpielerKapital()));

        Spieler gegenSpieler = null;

        switch (empfaengerAuswahl){
            case 4: gegenSpieler = gegenspielerListe.get(0);
                    break;
            case 5: gegenSpieler = gegenspielerListe.get(1);
                break;
            case 6: gegenSpieler = gegenspielerListe.get(2);
                break;
            case 7: gegenSpieler = gegenspielerListe.get(3);
                break;
            case 8: gegenSpieler = gegenspielerListe.get(4);
                break;
        }

        gegenSpieler.setSpielerKapital(roundDouble(gegenSpieler.getSpielerKapital() + betrag));
        databaseHandler.updateSpieler(eigenerSpieler);
        databaseHandler.updateSpieler(gegenSpieler);
        updateGegenSpielerCredit(gegenSpieler);

        JSONObject messageContent = messageParser.moneyTransactionToPlayerToJson(eigenerSpieler, gegenSpieler, betrag);

        GameMessage moneyTransactionToBankGameMessage = new GameMessage(GameMessage.MessageHeader.sendMoney, messageContent);

        String jsonString = messageParser.messageToJsonString(moneyTransactionToBankGameMessage);

        mGameConnectionService.mGameConnection.sendMessageToAllClients(jsonString);

        Toast.makeText(getApplicationContext(), "Du hast " + betrag + " M$ an " + gegenSpieler.getSpielerName() + " überwiesen!", Toast.LENGTH_SHORT).show();

        aktuellerBetragEditText.setText("");
        empfaengerAuswahl = 0;
    }

    public void updateGegenSpielerCredit(Spieler receiver) {

        ArrayList<Spieler> gegenSpielerListe = gegenspielerListe;

        for (int i = 0; i < gegenSpielerListe.size(); i++) {

            Spieler gegenSpieler = gegenSpielerListe.get(i);

            if (gegenSpielerListe.get(i).getSpielerIMEI().equals(receiver.getSpielerIMEI())) {
                gegenSpieler.setSpielerKapital(receiver.getSpielerKapital());

                gegenspielerListe.set(i, gegenSpieler);
                break;
            }
        }
    }

    public void spielerInit(){

        String imeiEigenerSpieler = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        gegenspielerListe = new ArrayList<>();

        for (String aktuelleSpielerIMEI: aktuelleSpielerIMEIs) {
            Spieler spieler = databaseHandler.getSpielerBySpielIdAndSpielerIMEI(aktuellesSpiel.getSpielID(), aktuelleSpielerIMEI);

            if(spieler.getSpielerIMEI().equals(imeiEigenerSpieler)){
                eigenerSpieler = spieler;
            }
            else{
                gegenspielerListe.add(spieler);
            }
        }
    }

    public void gegenspielerButtonViewsInit(){

        switch (gegenspielerListe.size()){
            case 1:
                gegenspieler1ButtonViewInit();
                break;
            case 2:
                gegenspieler1ButtonViewInit();
                gegenspieler2ButtonViewInit();
                break;
            case 3:
                gegenspieler1ButtonViewInit();
                gegenspieler2ButtonViewInit();
                gegenspieler3ButtonViewInit();
                break;
            case 4:
                gegenspieler1ButtonViewInit();
                gegenspieler2ButtonViewInit();
                gegenspieler3ButtonViewInit();
                gegenspieler4ButtonViewInit();
                break;
            case 5:
                gegenspieler1ButtonViewInit();
                gegenspieler2ButtonViewInit();
                gegenspieler3ButtonViewInit();
                gegenspieler4ButtonViewInit();
                gegenspieler5ButtonViewInit();
                break;
        }

    }

    public void gegenspieler1ButtonViewInit(){
        TextView gegenspieler1IconImageView = (TextView) findViewById(R.id.gegenspieler1ButtonView);
        Spieler spieler = gegenspielerListe.get(0);

        gegenspieler1IconImageView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), spieler.getSpielerFarbe()));
        gegenspieler1IconImageView.setText(String.valueOf(spieler.getSpielerName().charAt(0)));
        gegenspieler1IconImageView.setVisibility(View.VISIBLE);

        gegenspieler1IconImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                empfaengerAuswahl = 4;
                Toast.makeText(getApplicationContext(), "Spieler 1 ausgewählt", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void gegenspieler2ButtonViewInit(){
        TextView gegenspieler2IconImageView = (TextView) findViewById(R.id.gegenspieler2ButtonView);
        Spieler spieler = gegenspielerListe.get(1);

        gegenspieler2IconImageView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), spieler.getSpielerFarbe()));
        gegenspieler2IconImageView.setText(String.valueOf(spieler.getSpielerName().charAt(0)));
        gegenspieler2IconImageView.setVisibility(View.VISIBLE);

        gegenspieler2IconImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                empfaengerAuswahl = 5;
                Toast.makeText(getApplicationContext(), "Spieler 2 ausgewählt", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void gegenspieler3ButtonViewInit(){
        TextView gegenspieler3IconImageView = (TextView) findViewById(R.id.gegenspieler3ButtonView);
        Spieler spieler = gegenspielerListe.get(2);

        gegenspieler3IconImageView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), spieler.getSpielerFarbe()));
        gegenspieler3IconImageView.setText(String.valueOf(spieler.getSpielerName().charAt(0)));
        gegenspieler3IconImageView.setVisibility(View.VISIBLE);

        gegenspieler3IconImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                empfaengerAuswahl = 5;
                Toast.makeText(getApplicationContext(), "Spieler 3 ausgewählt", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void gegenspieler4ButtonViewInit(){
        TextView gegenspieler4IconImageView = (TextView) findViewById(R.id.gegenspieler4ButtonView);
        Spieler spieler = gegenspielerListe.get(3);

        gegenspieler4IconImageView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), spieler.getSpielerFarbe()));
        gegenspieler4IconImageView.setText(String.valueOf(spieler.getSpielerName().charAt(0)));
        gegenspieler4IconImageView.setVisibility(View.VISIBLE);

        gegenspieler4IconImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                empfaengerAuswahl = 6;
                Toast.makeText(getApplicationContext(), "Spieler 4 ausgewählt", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void gegenspieler5ButtonViewInit(){
        TextView gegenspieler5IconImageView = (TextView) findViewById(R.id.gegenspieler5ButtonView);
        Spieler spieler = gegenspielerListe.get(4);

        gegenspieler5IconImageView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), spieler.getSpielerFarbe()));
        gegenspieler5IconImageView.setText(String.valueOf(spieler.getSpielerName().charAt(0)));
        gegenspieler5IconImageView.setVisibility(View.VISIBLE);

        gegenspieler5IconImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                empfaengerAuswahl = 7;
                Toast.makeText(getApplicationContext(), "Spieler 5 ausgewählt", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void eigenerSpielerButtonViewInit(){
        TextView eigenerSpielerIconImageView = (TextView) findViewById(R.id.eigenerSpielerFarbeButtonView);

        eigenerSpielerIconImageView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), eigenerSpieler.getSpielerFarbe()));
        eigenerSpielerIconImageView.setText(String.valueOf(eigenerSpieler.getSpielerName().charAt(0)));

        eigenerSpielerIconImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                empfaengerAuswahl = 1;
                Toast.makeText(getApplicationContext(), "Eigener Spieler ausgewählt", Toast.LENGTH_SHORT).show();
            }
        });
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

                        if(neuesSpiel) {
                            JSONObject messageContent = messageParser.gameStatusToJson(eigenerSpieler, aktuellesSpiel);

                            GameMessage startGameMessage = new GameMessage(GameMessage.MessageHeader.gameEnd, messageContent);

                            String jsonString = messageParser.messageToJsonString(startGameMessage);

                            mGameConnectionService.mGameConnection.sendMessageToAllClients(jsonString);

                            Toast.makeText(getApplicationContext(), "Spiel beenden Nachricht gesendet", Toast.LENGTH_SHORT).show();
                        }

                        if(mServiceBound) {

                            mGameConnectionService.mGameConnection.tearDownGameServer();
                            mGameConnectionService.mGameConnection.tearDownGameClient();

                            Intent gameConnectionServiceIntent = new Intent(getApplicationContext(), GameConnectionService.class);
                            getApplicationContext().stopService(gameConnectionServiceIntent);
                            getApplicationContext().unbindService(mServiceConnection);

                            LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
                            broadcastManager.unregisterReceiver(messageReceiver);
                        }

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

    public double roundDouble(Double betrag){
        return Math.round(betrag*1000)/1000.0;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onBackPressed() {

    }

    public ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {

            mGameConnectionService = null;
            mServiceBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            GameConnectionService.MyBinder myBinder = (GameConnectionService.MyBinder) service;
            mGameConnectionService = myBinder.getService();
            mServiceBound = true;
        }
    };
}
