/**
 * 
 */
package com.autoStock.exchange.request.listener;

import com.autoStock.exchange.request.base.RequestHolder;
import com.autoStock.exchange.results.ExResultMarketData;
import com.autoStock.exchange.results.ExResultHistoricalData.ExResultSetHistoricalData;
import com.autoStock.exchange.results.ExResultMarketData.ExResultSetMarketData;
import com.autoStock.types.TypeQuoteSlice;

/**
 * @author Kevin Kowalewski
 *
 */
public interface RequestMarketDataListener {
		public void failed(RequestHolder requestHolder);
		public void receiveQuoteSlice(RequestHolder requestHolder, TypeQuoteSlice typeQuoteSlice);
		public void completed(RequestHolder requestHolder, ExResultSetMarketData exResultSetMarketData);
}
