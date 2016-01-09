package com.jamilaryan.provincialcost;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import static android.widget.SeekBar.OnSeekBarChangeListener;


public class MainActivity extends ActionBarActivity implements AdapterView.OnItemSelectedListener {

    //constants used when saving/restoring state
    private static final String INCOME_AMOUNT = "INCOME_AMOUNT";
    private static final String CUSTOM_EXPENSES = "CUSTOM_EXPENSES";

    private double provincialTaxAmount;         //for spinner
    private double currentIncomeAmount;     //currentBillTotal
    private int currentCustomExpenses;      //currentCustomPercent
    private EditText provTaxONEditText;     //tip10EditText
    private EditText totalONEditText;       //total10EditText
    private EditText provTaxABEditText;     //tip15EditText
    private EditText totalABEditText;       //total15EditText
    private EditText incomeEditText;        //billEditText
    private EditText provTaxBCEditText;     //tip20EditText
    private EditText totalBCEditText;       //total20EditText
    private TextView customExpTextView;     //customTipTextView
    private EditText expTotalEditText;      //tipCustomEditText
    private EditText balanceEditText;       //totalCustomEditText
    private Spinner spinner;                //spinner for province list
    double BCTotal;                         //total Balance after tax
    double BCTaxPercent;                    //total outgoing tax and costs
    //private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        //actionBar.setTitle("Currency Converter");
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.RED));
        actionBar.show();

        //imageView = (ImageView)findViewById(R.id.imageView);


        if (savedInstanceState==null)
        {
            currentIncomeAmount = 0.0;
            currentCustomExpenses=00;
        }
        else
        {
            //if device state changes, stored values will be received
            currentIncomeAmount = savedInstanceState.getDouble(INCOME_AMOUNT);

            currentCustomExpenses = savedInstanceState.getInt(CUSTOM_EXPENSES);

            //imageView.jumpDrawablesToCurrentState();

            //imageView.setVisibility(View.GONE);
        }

        //get reference to spinner & propagate
        spinner = (Spinner)findViewById(R.id.spinner);

        ArrayAdapter adapter = ArrayAdapter.createFromResource(this,R.array.provinces,
                android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        //get reference to ON,AB,BC and their totals
        provTaxONEditText = (EditText) findViewById(R.id.provTaxONEditText);
        totalONEditText = (EditText) findViewById(R.id.totalONEditText);
        provTaxABEditText = (EditText) findViewById(R.id.provTaxABEditText);
        totalABEditText = (EditText) findViewById(R.id.totalABEditText);
        provTaxBCEditText = (EditText) findViewById(R.id.provTaxBCEditText);
        totalBCEditText = (EditText) findViewById(R.id.totalBCEditText);

        //get the view to display the custom expense amount
        customExpTextView = (TextView)findViewById(R.id.customExpTextView);

        //get custom expense and totals
        expTotalEditText = (EditText) findViewById(R.id.expTotalEditText);
        balanceEditText = (EditText) findViewById(R.id.balanceEditText);

        //get the income edittext
        incomeEditText = (EditText) findViewById(R.id.incomeEditText);

        //incomeEditTextWatcher handles incomeEditText's onTextChanged event
        incomeEditText.addTextChangedListener(incomeEditTextWatcher);

        //get the seekbar used to set the custom expense amount
        SeekBar customSeekBar = (SeekBar)findViewById(R.id.otherExpSeekBar);
        customSeekBar.setOnSeekBarChangeListener(customSeekBarListener);

    }//end of OnCreate()


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem item= menu.findItem(R.id.action_settings);
        item.setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    //updates ON, AB, BC percent tax Edittexts
    private void updateStandard()
    {
        /*
        calculate total with federal tax
        This calculates the federal tax and sets texts
        */

        double ONTaxPercent = (currentIncomeAmount * .15);     //federal tax amount
        double ONTotal = currentIncomeAmount - ONTaxPercent;    //balance after federal

        //set provONTaxEditText's text to ONTaxPercent
        provTaxONEditText.setText(String.format("%.01f",ONTaxPercent));

        //set totalONEditText's text to ONTotal
        totalONEditText.setText(String.format("%.01f",ONTotal));

        /*
        calculate total with provincial tax
        This calculates the provincial tax and sets texts
        */
        double ABTaxPercent = (currentIncomeAmount-ONTaxPercent) * provincialTaxAmount;    //provincial tax amount
        double ABTotal = ONTotal - ABTaxPercent;                //balance after provincial

        //set provTaxABEditText's text to ABTaxPercent
        provTaxABEditText.setText(String.format("%.01f",ABTaxPercent));

        //set totalABEditText's text to ABTotal
        totalABEditText.setText(String.format("%.01f",ABTotal));

        /*
        calculate total with federal + provincial tax
        This calculates federal + provincial taxes
        */
               BCTaxPercent = ONTaxPercent + ABTaxPercent;              //federal + provincial taxes
               BCTotal = currentIncomeAmount - BCTaxPercent;            //balance after both taxes

        //set provTaxBCEditText's text to BCTaxPercent
        provTaxBCEditText.setText(String.format("%.01f",BCTaxPercent));

        //set totalBCEditText's text to BCTotal
        totalBCEditText.setText(String.format("%.01f",BCTotal));
    } // end method updateStandard

    private void updateCustom()
    {
        // set customExpTextView's text to match the position of the SeekBar
        customExpTextView.setText(currentCustomExpenses + "00 $");

        // calculate the custom expenses
        //outgoing is total of tax and costs
        double customExpAmount =
                BCTaxPercent + (currentCustomExpenses*100);

        // calculate the total income, including the expenses
        if (currentIncomeAmount>0) {
            double customTotalAmount = currentIncomeAmount - customExpAmount;

            // display the expense and total income amounts
            expTotalEditText.setText(String.format("%.01f", customExpAmount));
            balanceEditText.setText(String.format("%.01f", customTotalAmount));
        }
    } // end method updateCustom


    // save values of incomeEditText and customSeekBar
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putDouble(INCOME_AMOUNT,currentIncomeAmount);
        outState.putInt(CUSTOM_EXPENSES,currentCustomExpenses);
        //imageView.setVisibility(View.VISIBLE);
    } // end method onSaveInstanceState

    // called when the user changes the position of SeekBar
    private OnSeekBarChangeListener customSeekBarListener =
            new OnSeekBarChangeListener()
            {
                // update currentCustomExpense, then call updateCustom
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    // sets currentCustomExpense to position of the SeekBar's thumb
                    currentCustomExpenses = seekBar.getProgress();
                    updateCustom(); // update EditTexts for custom expense and total
                } // end method onPrgressChanged

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            }; // end customSeekBarListener

    private TextWatcher incomeEditTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            // convert incomeEditText's text to a double
            try {
                currentIncomeAmount = Double.parseDouble(s.toString());
            } // end try
            catch (NumberFormatException e)
            {
                currentIncomeAmount = 0.0;
            } // end catch

            // update the standard and custom expense editTexts
            updateStandard(); // update the ON, AB, BC EditTexts
            updateCustom(); // update the custom expense EditTexts
        } // end method onTextChanged

        @Override
        public void afterTextChanged(Editable s) {

        } // end method afterTextChanged
    }; // end incomeEditTextWatcher

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        // assigns provincial taxes
        switch (position) {
            case 0:  provincialTaxAmount = .1000;
                break;
            case 1:  provincialTaxAmount = .0506;
                break;
            case 2:  provincialTaxAmount = .1080;
                break;
            case 3:  provincialTaxAmount = .0968;
                break;
            case 4:  provincialTaxAmount = .0770;
                break;
            case 5:  provincialTaxAmount = .0590;
                break;
            case 6:  provincialTaxAmount = .0879;
                break;
            case 7:  provincialTaxAmount = .0400;
                break;
            case 8:  provincialTaxAmount = .0505;
                break;
            case 9:  provincialTaxAmount = .0980;
                break;
            case 10: provincialTaxAmount = .1600;
                break;
            case 11: provincialTaxAmount = .1100;
                break;
            case 12: provincialTaxAmount = .0704;
                break;
        }//end of switch-case

        //message
        TextView myText = (TextView) view;
        Toast.makeText(this, "You selected: " + myText.getText() +
                " Tax Rate: " + provincialTaxAmount*100, Toast.LENGTH_SHORT).show();

        ((TextView) parent.getChildAt(0)).setTextColor(Color.RED);
        //((TextView) parent.getChildAt(0)).setTextSize(15);

        updateStandard();
        updateCustom();

    }//end of OnSelectedItem

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}// end of class
