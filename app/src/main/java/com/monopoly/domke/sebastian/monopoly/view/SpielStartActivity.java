package com.monopoly.domke.sebastian.monopoly.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.monopoly.domke.sebastian.monopoly.R;
import com.monopoly.domke.sebastian.monopoly.common.Spiel;
import com.monopoly.domke.sebastian.monopoly.common.Spieler;
import com.monopoly.domke.sebastian.monopoly.database.DatabaseHandler;

public class SpielStartActivity extends AppCompatActivity {

    String eigenerSpielerIP;
    int aktuellesSpielID;
    Spiel aktuellesSpiel;
    Spieler eigenerSpieler;
    TextView aktuellesKapitalEigenerSpielerTextView;
    EditText aktuellerBetragEditText;
    int hypothek = 0;
    int empfaengerAuswahl = 0;

    DatabaseHandler databaseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spiel_start);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        databaseHandler = new DatabaseHandler(this);

        Intent intent = getIntent();

        eigenerSpielerIP = intent.getStringExtra("eigenerSpielerIpAdresse");
        aktuellesSpielID = intent.getIntExtra("aktuellesSpielID", 0);

        aktuellesSpiel = databaseHandler.getSpielByID(aktuellesSpielID);

        eigenerSpieler = databaseHandler.getSpielerBySpielIdAndSpielerIp(aktuellesSpielID, eigenerSpielerIP);

        init();

        //Todo Nachrichten an die Spieler bei Transaktionen und Schnelltasten

        //Todo Spiel speichern und Spiel beenden Funktion hinzufügen
    }

    public void init(){
        aktuellesKapitalEigenerSpielerTextView = (TextView) findViewById(R.id.deinKapitalTextView);
        aktuellesKapitalEigenerSpielerTextView.setText(String.valueOf(eigenerSpieler.getSpielerKapital()));

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
                Toast.makeText(getApplicationContext(), "Spieler 2 ausgewählt", Toast.LENGTH_SHORT).show();
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
                eigenerSpieler.setSpielerKapital(eigenerSpieler.getSpielerKapital() + 2000000);
                aktuellesKapitalEigenerSpielerTextView.setText(String.valueOf(eigenerSpieler.getSpielerKapital()));
            }
        });

        erhalteFreiParkenButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eigenerSpieler.setSpielerKapital(eigenerSpieler.getSpielerKapital() + aktuellesSpiel.getFreiParken());
                aktuellesKapitalEigenerSpielerTextView.setText(String.valueOf(eigenerSpieler.getSpielerKapital()));
                aktuellesSpiel.setFreiParken(0);
            }
        });

        erhalteHypothekButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(aktuellerBetragEditText.getText().toString().length() != 0) {

                    eigenerSpieler.setSpielerKapital(Integer.valueOf(aktuellerBetragEditText.getText().toString()) + eigenerSpieler.getSpielerKapital());
                    aktuellesKapitalEigenerSpielerTextView.setText(String.valueOf(eigenerSpieler.getSpielerKapital()));

                    hypothek += Integer.valueOf(aktuellerBetragEditText.getText().toString());
                    aktuellerBetragEditText.setText("");
                }
                else{
                    Toast.makeText(getApplicationContext(), "Bitte zuerst gewünschten Betrag für die Hypothek eingeben", Toast.LENGTH_SHORT).show();
                }
            }
        });

        bezahleGefaengnisButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eigenerSpieler.setSpielerKapital(eigenerSpieler.getSpielerKapital() - 500000);
                aktuellesKapitalEigenerSpielerTextView.setText(String.valueOf(eigenerSpieler.getSpielerKapital()));
                aktuellesSpiel.setFreiParken(aktuellesSpiel.getFreiParken() + 500000);
            }
        });

        bezahleHypothekButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(aktuellerBetragEditText.getText().toString().length() != 0) {

                    eigenerSpieler.setSpielerKapital(eigenerSpieler.getSpielerKapital() - Integer.valueOf(aktuellerBetragEditText.getText().toString()));
                    aktuellesKapitalEigenerSpielerTextView.setText(String.valueOf(eigenerSpieler.getSpielerKapital()));

                    hypothek -= Integer.valueOf(aktuellerBetragEditText.getText().toString());
                    aktuellerBetragEditText.setText("");
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

        eigenerSpieler.setSpielerKapital(Integer.valueOf(aktuellerBetragEditText.getText().toString()) + eigenerSpieler.getSpielerKapital());
        aktuellesKapitalEigenerSpielerTextView.setText(String.valueOf(eigenerSpieler.getSpielerKapital()));

        aktuellerBetragEditText.setText("");
        empfaengerAuswahl = 0;


    }

    public void transaktionAnDieBank() {

        eigenerSpieler.setSpielerKapital(eigenerSpieler.getSpielerKapital() - Integer.valueOf(aktuellerBetragEditText.getText().toString()));
        aktuellesKapitalEigenerSpielerTextView.setText(String.valueOf(eigenerSpieler.getSpielerKapital()));

        aktuellerBetragEditText.setText("");
        empfaengerAuswahl = 0;
    }

    public void transaktionInDieMitte() {

        eigenerSpieler.setSpielerKapital(eigenerSpieler.getSpielerKapital() - Integer.valueOf(aktuellerBetragEditText.getText().toString()));
        aktuellesKapitalEigenerSpielerTextView.setText(String.valueOf(eigenerSpieler.getSpielerKapital()));

        aktuellesSpiel.setFreiParken(aktuellesSpiel.getFreiParken() + Integer.valueOf(aktuellerBetragEditText.getText().toString()));

        aktuellerBetragEditText.setText("");
        empfaengerAuswahl = 0;
    }

    public void transaktionAnEinenSpieler() {

        eigenerSpieler.setSpielerKapital(eigenerSpieler.getSpielerKapital() - Integer.valueOf(aktuellerBetragEditText.getText().toString()));
        aktuellesKapitalEigenerSpielerTextView.setText(String.valueOf(eigenerSpieler.getSpielerKapital()));

        //Todo Überweisung an den ausgewählten Spieler

        aktuellerBetragEditText.setText("");
        empfaengerAuswahl = 0;
    }

    public void gegenspielerInit(){
        //Todo Initialisieren der Gegenspieler
    }


}
