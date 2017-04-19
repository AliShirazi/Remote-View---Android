package com.innerID.RemoteView;

import com.innerID.RemoteDemo.R;

import android.app.DialogFragment;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class AboutDialog extends DialogFragment
{

	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		super.onCreateView(inflater, container, savedInstanceState);
		View aboutDialogView = inflater.inflate(R.layout.aboutdialog, container);
		PackageInfo pInfo;
		String version = null;
		try 
		{
			pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
			version = pInfo.versionName;
		} 
		catch (NameNotFoundException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.getDialog().setTitle("About");
		TextView appVersion = (TextView) aboutDialogView.findViewById(R.id.appVersion);
		appVersion.setText(getResources().getString(R.string.app_name) + " " + version);
		Button closeAboutDialog = (Button) aboutDialogView.findViewById(R.id.closeAbout);
		closeAboutDialog.setOnClickListener(new View.OnClickListener() 
		{
			
			@Override
			public void onClick(View v) 
			{
				dismiss();
				
			}
		});
		return aboutDialogView;
		
	}
}
