package net.osmand.plus.quickaction;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;

import net.osmand.plus.IconsCache;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;

/**
 * Created by rosty on 12/27/16.
 */

public class CreateEditActionDialog extends DialogFragment {

    public static final String TAG = CreateEditActionDialog.class.getSimpleName();

    public static final String KEY_ACTION_ID = "action_id";
    public static final String KEY_ACTION_TYPE = "action_type";
    public static final String KEY_ACTION_IS_NEW = "action_is_new";

    public static CreateEditActionDialog newInstance(long id) {

        Bundle args = new Bundle();
        args.putLong(KEY_ACTION_ID, id);

        CreateEditActionDialog dialog = new CreateEditActionDialog();
        dialog.setArguments(args);

        return dialog;
    }

    public static CreateEditActionDialog newInstance(int type) {

        Bundle args = new Bundle();
        args.putInt(KEY_ACTION_TYPE, type);

        CreateEditActionDialog dialog = new CreateEditActionDialog();
        dialog.setArguments(args);

        return dialog;
    }

    private QuickActionRegistry quickActionRegistry;
    private QuickAction action;

    private boolean isNew;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog = new Dialog(new ContextThemeWrapper(getActivity(), R.style.OsmandLightTheme), getTheme());
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(DialogFragment.STYLE_NORMAL, R.style.OsmandLightTheme);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.quick_action_create_edit_dialog, parent, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        quickActionRegistry = ((MapActivity) getActivity()).getMapLayers().getQuickActionRegistry();

        long actionId =  savedInstanceState == null
                ? getArguments().getLong(KEY_ACTION_ID)
                : savedInstanceState.getLong(KEY_ACTION_ID);

        int type = savedInstanceState == null
                ? getArguments().getInt(KEY_ACTION_TYPE)
                : savedInstanceState.getInt(KEY_ACTION_TYPE);

        isNew = savedInstanceState == null
                ? isNew = actionId == 0
                : savedInstanceState.getBoolean(KEY_ACTION_IS_NEW);

        action = QuickActionFactory.produceAction(isNew
                ? QuickActionFactory.newActionByType(type)
                : quickActionRegistry.getQuickAction(actionId));

        setupToolbar(view);
        setupHeader(view, savedInstanceState);
        setupFooter(view);

        action.drawUI((ViewGroup) view.findViewById(R.id.container), (MapActivity) getActivity());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLong(KEY_ACTION_ID, action.getId());
        outState.putInt(KEY_ACTION_TYPE, action.type);
        outState.putBoolean(KEY_ACTION_IS_NEW, isNew);
    }

    private void setupToolbar(View root) {

        Toolbar toolbar = (Toolbar) root.findViewById(R.id.toolbar);

        toolbar.setTitle(isNew
                ? R.string.quick_action_new_action
                : R.string.quick_action_edit_action);

        toolbar.setTitleTextColor(Color.WHITE);

        toolbar.setNavigationIcon(getIconsCache().getIcon(
                R.drawable.abc_ic_ab_back_mtrl_am_alpha,
                R.color.color_white));

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private void setupHeader(View root, Bundle savedInstanceState){

        ImageView image = (ImageView) root.findViewById(R.id.image);
        EditText name = (EditText) root.findViewById(R.id.name);

        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                action.setName(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        if (savedInstanceState == null) name.setText(action.getName(getContext()));
        else action.setName(name.getText().toString());

        image.setImageResource(action.iconRes);
    }

    private void setupFooter(final View root){

        root.findViewById(R.id.btnApply).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                action.fillParams(((ViewGroup) root.findViewById(R.id.container)).getChildAt(0));

                if (isNew) quickActionRegistry.addQuickAction(action);
                else quickActionRegistry.updateQuickAction(action);

                quickActionRegistry.notifyUpdates();

                dismiss();
            }
        });
    }

    private OsmandApplication getApplication(){
        return (OsmandApplication)(getContext().getApplicationContext());
    }

    private IconsCache getIconsCache(){
        return getApplication().getIconsCache();
    }
}
