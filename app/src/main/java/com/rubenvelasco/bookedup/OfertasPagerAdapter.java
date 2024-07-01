package com.rubenvelasco.bookedup;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.List;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class OfertasPagerAdapter extends PagerAdapter {
    private final Context context;
    private final List<Servicio> servicios;

    public OfertasPagerAdapter(Context context, List<Servicio> servicios) {
        this.context = context;
        this.servicios = servicios;
    }

    @Override
    public int getCount() {
        return servicios.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.layout_ofertas, container, false);

        Servicio servicio = servicios.get(position);

        ImageView imagen = view.findViewById(R.id.imagen);
        TextView nombre = view.findViewById(R.id.nombre);
        TextView subtitle = view.findViewById(R.id.tvSubtitle);
        TextView ofertas = view.findViewById(R.id.ofertas);
        TextView horario = view.findViewById(R.id.horario);
        Button btnMasInformacion = view.findViewById(R.id.btnMasInformacion);

        nombre.setText(servicio.getNombre());
        ofertas.setText(String.valueOf(servicio.getOfertas()));
        horario.setText(servicio.getHorario());
        subtitle.setText("Ofertas");

        // Concatenar las ofertas en un solo string con saltos de línea
        StringBuilder ofertasStringBuilder = new StringBuilder();
        for (String oferta : servicio.getOfertas()) {
            ofertasStringBuilder.append(oferta).append("\n");
        }
        String ofertasString = ofertasStringBuilder.toString().trim();

        btnMasInformacion.setOnClickListener(v -> {
            Intent intent = new Intent(context, ServiceScreen.class);
            intent.putExtra("nombre", servicio.getNombre());
            intent.putExtra("descripcion", ofertasString);
            intent.putExtra("horario", servicio.getHorario());
            intent.putExtra("imageUrl", servicio.getImageUrl());
            intent.putExtra("categoria", servicio.getCategoria());
            intent.putExtra("profesional", servicio.getProfesional());
            intent.putExtra("ubicacion", servicio.getUbicacion());
            // Agregar la lista de días disponibles al intent
            intent.putStringArrayListExtra("dias", servicio.getDias());
            context.startActivity(intent);
        });

        int radius = 55;
        int margin = 5;
        Transformation transformation = new RoundedCornersTransformation(radius, margin);

        Picasso.get()
                .load(servicio.getImageUrl())
                .resize(300, 300)
                .centerCrop()
                .transform(transformation)
                .into(imagen);

        container.addView(view);
        view.setTag(servicio);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}