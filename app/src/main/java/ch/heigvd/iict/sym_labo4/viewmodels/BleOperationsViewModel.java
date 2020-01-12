package ch.heigvd.iict.sym_labo4.viewmodels;

import android.app.Application;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Calendar;
import java.util.UUID;

import ch.heigvd.iict.sym_labo4.BleActivity;
import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.BleManagerCallbacks;
import no.nordicsemi.android.ble.data.Data;

import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT16;
import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT8;

public class BleOperationsViewModel extends AndroidViewModel {

    public  UUID UUID_SERVICE_CURRENT_TIME = ParcelUuid.fromString( "00001805-0000-1000-8000-00805f9b34fb").getUuid();
    public  UUID UUID_CURRENT_TIME = ParcelUuid.fromString( "00002A2B-0000-1000-8000-00805f9b34fb").getUuid();
    public  UUID UUID_SERVICE_CUSTOM_SYM = ParcelUuid.fromString( "3c0a1000-281d-4b48-b2a7-f15579a1c38f").getUuid();
    public  UUID UUID_INTEGER = ParcelUuid.fromString( "3c0a1001-281d-4b48-b2a7-f15579a1c38f").getUuid();
    public  UUID UUID_TEMPERATURE = ParcelUuid.fromString( "3c0a1002-281d-4b48-b2a7-f15579a1c38f").getUuid();
    public  UUID UUID_BTN = ParcelUuid.fromString( "3c0a1003-281d-4b48-b2a7-f15579a1c38f").getUuid();

    private static final String TAG = BleOperationsViewModel.class.getSimpleName();

    private MySymBleManager ble = null;
    private BluetoothGatt mConnection = null;

    //live data - observer
    private final MutableLiveData<Boolean> mIsConnected = new MutableLiveData<>();
    public LiveData<Boolean> isConnected() {
        return mIsConnected;
    }

    private final MutableLiveData<Integer> integer = new MutableLiveData<>();
    public LiveData<Integer> getInteger() {
        return integer;
    }

    private final MutableLiveData<Integer> temperature = new MutableLiveData<>();
    public LiveData<Integer> getTemperature() {
        return temperature;
    }

    private final MutableLiveData<Integer> nbBtnClicked = new MutableLiveData<>();
    public LiveData<Integer> getNbBtnClicked() { return nbBtnClicked; }

    private final MutableLiveData<Calendar>  currentTime = new MutableLiveData<>();
    public LiveData<Calendar> getCurrentTime() { return currentTime; }

    //references to the Services and Characteristics of the SYM Pixl
    private BluetoothGattService timeService = null, symService = null;
    private BluetoothGattCharacteristic currentTimeChar = null, integerChar = null, temperatureChar = null, buttonClickChar = null;

    public BleOperationsViewModel(Application application) {
        super(application);
        this.mIsConnected.setValue(false); //to be sure that it's never null
        this.ble = new MySymBleManager();
        this.ble.setGattCallbacks(this.bleManagerCallbacks);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "onCleared");
        this.ble.disconnect();
    }

    public void connect(BluetoothDevice device) {
        Log.d(TAG, "User request connection to: " + device);
        if(!mIsConnected.getValue()) {
            this.ble.connect(device)
                    .retry(1, 100)
                    .useAutoConnect(false)
                    .enqueue();
        }
    }

    public void disconnect() {
        Log.d(TAG, "User request disconnection");
        this.ble.disconnect();
        if(mConnection != null) {
            mConnection.disconnect();
        }
    }
    /* TODO
        vous pouvez placer ici les différentes méthodes permettant à l'utilisateur
        d'interagir avec le périphérique depuis l'activité
     */
    public boolean writeCurrentTime(){
        if (currentTimeChar == null) {
            return false;
        }
        Calendar calendar = Calendar.getInstance();

        //int year = calendar.get(Calendar.YEAR );
        byte[] value = new byte[8];
        value[0] = (byte)(calendar.get(Calendar.YEAR ));
        value[1] = (byte)(calendar.get(Calendar.YEAR ) >> 8);
        value[2] = (byte)(calendar.get(Calendar.MONTH) + 1);
        value[3] = (byte)(calendar.get(Calendar.DAY_OF_MONTH));
        value[4] = (byte)calendar.get(Calendar.HOUR_OF_DAY);
        value[5] = (byte)calendar.get(Calendar.MINUTE);
        value[6] = (byte)calendar.get(Calendar.SECOND);

        currentTimeChar.setValue(value);


        return mConnection.writeCharacteristic(currentTimeChar);
    }

    public boolean writeInteger(Integer i){
        if (integerChar == null) {
            return false;
        }
        byte[] value = new byte[1];
        value[0] = i.byteValue();
        integerChar.setValue(value);
        return mConnection.writeCharacteristic(integerChar);
    }

    public boolean readTemperature() {
        if(!isConnected().getValue() || temperatureChar == null){
            return false;
        }
        return ble.readTemperature();
    }

    private BleManagerCallbacks bleManagerCallbacks = new BleManagerCallbacks() {
        @Override
        public void onDeviceConnecting(@NonNull BluetoothDevice device) {
            Log.d(TAG, "onDeviceConnecting");
            mIsConnected.setValue(false);
        }

        @Override
        public void onDeviceConnected(@NonNull BluetoothDevice device) {
            Log.d(TAG, "onDeviceConnected");
            mIsConnected.setValue(true);
        }

        @Override
        public void onDeviceDisconnecting(@NonNull BluetoothDevice device) {
            Log.d(TAG, "onDeviceDisconnecting");
            mIsConnected.setValue(false);
        }

        @Override
        public void onDeviceDisconnected(@NonNull BluetoothDevice device) {
            Log.d(TAG, "onDeviceDisconnected");
            mIsConnected.setValue(false);
        }

        @Override
        public void onLinkLossOccurred(@NonNull BluetoothDevice device) {
            Log.d(TAG, "onLinkLossOccurred");
        }

        @Override
        public void onServicesDiscovered(@NonNull BluetoothDevice device, boolean optionalServicesFound) {
            Log.d(TAG, "onServicesDiscovered");
        }

        @Override
        public void onDeviceReady(@NonNull BluetoothDevice device) {
            Log.d(TAG, "onDeviceReady");
        }

        @Override
        public void onBondingRequired(@NonNull BluetoothDevice device) {
            Log.w(TAG, "onBondingRequired");
        }

        @Override
        public void onBonded(@NonNull BluetoothDevice device) {
            Log.d(TAG, "onBonded");
        }

        @Override
        public void onBondingFailed(@NonNull BluetoothDevice device) {
            Log.e(TAG, "onBondingFailed");
        }

        @Override
        public void onError(@NonNull BluetoothDevice device, @NonNull String message, int errorCode) {
            Log.e(TAG, "onError:" + errorCode);
        }

        @Override
        public void onDeviceNotSupported(@NonNull BluetoothDevice device) {
            Log.e(TAG, "onDeviceNotSupported");
            Toast.makeText(getApplication(), "Device not supported", Toast.LENGTH_SHORT).show();
        }
    };

    /*
     *  This class is used to implement the protocol to communicate with the BLE device
     */
    private class MySymBleManager extends BleManager<BleManagerCallbacks> {

        private MySymBleManager() {
            super(getApplication());
        }

        @Override
        public BleManagerGattCallback getGattCallback() { return mGattCallback; }

        /**
         * BluetoothGatt callbacks object.
         */
        private final BleManagerGattCallback mGattCallback = new BleManagerGattCallback() {

            @Override
            public boolean isRequiredServiceSupported(@NonNull final BluetoothGatt gatt) {
                mConnection = gatt; //trick to force disconnection
                Log.d(TAG, "isRequiredServiceSupported - discovered services:");

                for(BluetoothGattService service : gatt.getServices()){
                    if( service.getUuid().equals(UUID_SERVICE_CURRENT_TIME)){
                        timeService = service;
                        currentTimeChar = timeService.getCharacteristic( UUID_CURRENT_TIME);
                    }
                    else if ( service.getUuid().equals(UUID_SERVICE_CUSTOM_SYM)){
                        symService = service;
                        integerChar = symService.getCharacteristic(UUID_INTEGER);
                        temperatureChar = symService.getCharacteristic(UUID_TEMPERATURE);
                        buttonClickChar = symService.getCharacteristic(UUID_BTN);

                    }
                }

                /* TODO
                    - Nous devons vérifier ici que le périphérique auquel on vient de se connecter possède
                      bien tous les services et les caractéristiques attendues, on vérifiera aussi que les
                      caractéristiques présentent bien les opérations attendues
                    - On en profitera aussi pour garder les références vers les différents services et
                      caractéristiques (déclarés en lignes 33 et 34)
                 */

                //FIXME si tout est OK, on retourne true, sinon la librairie appelera la méthode onDeviceNotSupported()
                //pas besoin de vérifier les services on a pu setter les charactéristiques que si il y a le service
                return buttonClickChar != null && temperatureChar != null && integerChar != null && currentTimeChar != null;
            }

            @Override
            protected void initialize() {
                /* TODO
                    Ici nous somme sûr que le périphérique possède bien tous les services et caractéristiques
                    attendus et que nous y sommes connectés. Nous pouvous effectuer les premiers échanges BLE:
                    Dans notre cas il s'agit de s'enregistrer pour recevoir les notifications proposées par certaines
                    caractéristiques, on en profitera aussi pour mettre en place les callbacks correspondants.
                 */

                writeCurrentTime();

                setNotificationCallback(currentTimeChar).with((device, data) -> {
                    readCurrentTime(data);
                });

                setNotificationCallback(buttonClickChar).with((device, data) -> {
                    readNbButtonClicked(data);
                });

                enableNotifications(currentTimeChar).enqueue();
                enableNotifications(buttonClickChar).enqueue();
            }

            @Override
            protected void onDeviceDisconnected() {
                //we reset services and characteristics
                timeService = null;
                currentTimeChar = null;

                symService = null;
                integerChar = null;
                temperatureChar = null;
                buttonClickChar = null;
            }
        };

        public boolean readTemperature() {
            /* TODO on peut effectuer ici la lecture de la caractéristique température
                la valeur récupérée sera envoyée à l'activité en utilisant le mécanisme
                des MutableLiveData
                On placera des méthodes similaires pour les autres opérations...
            */

            if(temperatureChar == null){
                return false;
            }

            readCharacteristic(temperatureChar).with((device, data) -> {

                temperature.setValue( data.getIntValue(Data.FORMAT_UINT16, 0));
            }).enqueue();



            return true;
        }

        private void readCurrentTime(Data data){
            Calendar calendar = Calendar.getInstance();

            calendar.set(Calendar.YEAR, data.getIntValue(Data.FORMAT_UINT16,0));
            calendar.set(Calendar.MONTH, data.getIntValue(Data.FORMAT_UINT8,2) - 1);
            calendar.set(Calendar.DAY_OF_MONTH, data.getIntValue(Data.FORMAT_UINT8,3));
            calendar.set(Calendar.HOUR_OF_DAY, data.getIntValue(Data.FORMAT_UINT8,4));
            calendar.set(Calendar.MINUTE, data.getIntValue(Data.FORMAT_UINT8,5));
            calendar.set(Calendar.SECOND, data.getIntValue(Data.FORMAT_UINT8,6));

            currentTime.setValue(calendar);
        }

        private void readNbButtonClicked(Data data){
            nbBtnClicked.setValue(data.getIntValue(Data.FORMAT_UINT8, 0));
        }

    }
}
