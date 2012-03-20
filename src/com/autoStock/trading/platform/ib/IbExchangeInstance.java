/**
 * 
 */
package com.autoStock.trading.platform.ib;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.autoStock.Co;
import com.autoStock.exchange.request.base.RequestHolder;
import com.autoStock.internal.Config;
import com.autoStock.trading.platform.ib.core.Contract;
import com.autoStock.trading.platform.ib.core.EClientSocket;
import com.autoStock.trading.platform.ib.core.Order;
import com.autoStock.trading.platform.ib.core.ScannerSubscription;
import com.autoStock.trading.types.TypeHistoricalData;
import com.autoStock.trading.types.TypeMarketData;
import com.autoStock.trading.types.TypePosition;
import com.autoStock.trading.types.TypeRealtimeData;

/**
 * @author Kevin Kowalewski
 *
 */
public class IbExchangeInstance {
	public IbExchangeWrapper ibExchangeWrapper;
	public IbExchangeClientSocket ibExchangeClientSocket;
	
	public void init(){
		ibExchangeWrapper = new IbExchangeWrapper();
		ibExchangeClientSocket = new IbExchangeClientSocket();
		
		try {
			ibExchangeClientSocket.init(ibExchangeWrapper);
			ibExchangeClientSocket.connect();
		}catch(Exception e){} //e.printStackTrace();
	}
	
	public EClientSocket getEclientSocket(){
		return ibExchangeClientSocket.eClientSocket;
	}
	
	public void getAccountUpdates(){
		ibExchangeClientSocket.eClientSocket.reqAccountUpdates(true, Config.plIbUsername);
	}

	public void getOpenOrders(){
		ibExchangeClientSocket.eClientSocket.reqOpenOrders();
	}
	
	public void placeBuyOrder(TypePosition typePosition, RequestHolder requestHolder){
		Contract contract = new Contract();
		Order order = new Order();
		contract.m_exchange = "NYSE";
		contract.m_symbol = typePosition.symbol;
		contract.m_secType = "STK";
		contract.m_currency = "USD";
		order.m_action = "BUY";
		order.m_orderType = "MKT";
		order.m_auxPrice = 0.01;
		order.m_totalQuantity = typePosition.units;
		
		ibExchangeClientSocket.eClientSocket.placeOrder(requestHolder.requestId, contract, order);
	}
	
	public void placeSellOrder(TypePosition typePosition, RequestHolder requestHolder){
		Contract contract = new Contract();
		Order order = new Order();
		contract.m_exchange = "NYSE";
		contract.m_symbol = typePosition.symbol;
		contract.m_secType = "STK";
		contract.m_currency = "USD";
		order.m_action = "SELL";
		order.m_orderType = "MKT";
		order.m_auxPrice = 0.1;
		order.m_totalQuantity = typePosition.units;
		
		ibExchangeClientSocket.eClientSocket.placeOrder(requestHolder.requestId, contract, order);
	}
	
	public void placeShortOrder(TypePosition typePosition, RequestHolder requestHolder){
		Contract contract = new Contract();
		Order order = new Order();
		contract.m_exchange = "NYSE";
		contract.m_symbol = typePosition.symbol;
		contract.m_secType = "STK";
		contract.m_currency = "USD";
		order.m_action = "SELL";
		order.m_orderType = "MKT";
		order.m_auxPrice = 0.1;
		order.m_totalQuantity = typePosition.units;
		
		ibExchangeClientSocket.eClientSocket.placeOrder(requestHolder.requestId, contract, order);
	}
	
	public void getScanner(){
		ScannerSubscription scanner = new ScannerSubscription();
		scanner.numberOfRows(50);
		scanner.instrument("STK");
		scanner.locationCode("STK.NYSE");
		scanner.scanCode("TOP_PERC_GAIN");
		scanner.averageOptionVolumeAbove(0);
		ibExchangeClientSocket.eClientSocket.reqScannerSubscription(1, scanner);
	}
	
	public void getRealtimeData(TypeRealtimeData typeRealtimeData, RequestHolder requestHolder){
		//Co.println("Request id: " + requestHolder.requestId);
		Contract contract = new Contract();
		contract.m_exchange = "CHIXJ";
		contract.m_symbol = typeRealtimeData.symbol;
		contract.m_secType = typeRealtimeData.securityType;
		contract.m_currency = "JPY";
		ibExchangeClientSocket.eClientSocket.reqRealTimeBars(requestHolder.requestId, contract, 5, "TRADES", false);
	}
	
	public void getMarketData(TypeMarketData typeMarketData, RequestHolder requestHolder){
		//Co.println("Request id: " + requestHolder.requestId);
		Contract contract = new Contract();
		contract.m_exchange = "CHIXJ";
		contract.m_symbol = typeMarketData.symbol;
		contract.m_secType = typeMarketData.securityType;
		contract.m_currency = "JPY";
		ibExchangeClientSocket.eClientSocket.reqMktData(requestHolder.requestId, contract, "104,165,225", false);
	}
	
	public void getHistoricalPrice(TypeHistoricalData typeHistoricalData, RequestHolder requestHolder){
		//Co.println("Request id: " + requestHolder.requestId);
		Contract contract = new Contract();
		contract.m_exchange = "Smart";
		contract.m_symbol = typeHistoricalData.symbol;
		contract.m_secType = typeHistoricalData.securityType;
		contract.m_currency = "USD";
		String endDate = new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(typeHistoricalData.endDate) + " est";
		String duration = String.valueOf(typeHistoricalData.duration) + " S";
		ibExchangeClientSocket.eClientSocket.reqHistoricalData(requestHolder.requestId, contract, endDate, duration, typeHistoricalData.resolution.barSize, "TRADES", 1, 2);
	}
}
