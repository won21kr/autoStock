package com.autoStock.backtest;

import com.autoStock.account.AccountProvider;
import com.autoStock.backtest.BacktestUtils.BacktestResultTransactionDetails;

/**
 * @author Kevin Kowalewski
 *
 */
public class BacktestEvaluation {
	public BacktestResultTransactionDetails backtestResultTransactionDetails;
	public double accountBalance;
	public double percentGain;
	public double percentTradeWin;
	public double percentTradeLoss;
	public String stringRepresentation;
	
	public double getScore(){
//		return accountBalance - AccountProvider.getInstance().defaultBalance;
		return (accountBalance - AccountProvider.getInstance().defaultBalance) * percentTradeWin;
	}
}
