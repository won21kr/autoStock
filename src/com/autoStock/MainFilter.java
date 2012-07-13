package com.autoStock;

import com.autoStock.exchange.ExchangeController;
import com.autoStock.exchange.request.RequestMarketScanner;
import com.autoStock.exchange.request.base.RequestHolder;
import com.autoStock.exchange.request.listener.RequestMarketScannerListener;
import com.autoStock.exchange.results.ExResultMarketScanner.ExResultSetMarketScanner;
import com.autoStock.types.Exchange;

/**
 * @author Kevin Kowalewski
 *
 */
public class MainFilter {
	private Exchange exchange;
	private RequestMarketScanner requestMarketScanner;
	
	public MainFilter(Exchange exchange) {
		this.exchange = exchange;		
		
		requestMarketScanner = new RequestMarketScanner(
			new RequestHolder(new RequestMarketScannerListener(){
				@Override
				public void failed(RequestHolder requestHolder) {
					Co.println("Failed to get market filter");
				}

				@Override
				public void completed(RequestHolder requestHolder, ExResultSetMarketScanner exResultSetMarketScanner) {
					Co.println("Got market filter information OK");
				}
			}
		));
	}
}