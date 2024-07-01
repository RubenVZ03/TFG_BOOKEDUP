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

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class ServicioPagerAdapter extends PagerAdapter {

    private Context mContext;
    private List<Servicio> mServicios;

    public ServicioPagerAdapter(Context context, List<Servicio> servicios) {
        mContext = context;
        mServicios = servicios;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.layout_servicio, container, false);

        Servicio servicio = mServicios.get(position);

        ImageView imagenImageView = view.findViewById(R.id.imagen);
        TextView nombreTextView = view.findViewById(R.id.nombre);
        TextView descripcionTextView = view.findViewById(R.id.descripcion);
        TextView horarioTextView = view.findViewById(R.id.horario);
        Button btnMasInformacion = view.findViewById(R.id.btnMasInformacion);

        // Cargar la imagen utilizando Picasso y redimensionarla con esquinas redondeadas
        int radius = 55; // El radio de las esquinas en píxeles
        int margin = 15; // El margen en píxeles
        Transformation transformation = new RoundedCornersTransformation(radius, margin);

        Picasso.get()
                .load(servicio.getImageUrl())
                .resize(300, 300)  // Cambia el tamaño según tus necesidades
                .centerCrop()
                .transform(transformation)
                .into(imagenImageView);

        nombreTextView.setText(servicio.getNombre());
        descripcionTextView.setText(servicio.getDescripcion());
        horarioTextView.setText(servicio.getHorario());

        // Configurar el botón "Más Información"
        btnMasInformacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ServiceScreen.class);
                intent.putExtra("nombre", servicio.getNombre());
                intent.putExtra("descripcion", servicio.getDescripcion());
                intent.putExtra("horario", servicio.getHorario());
                intent.putExtra("imageUrl", servicio.getImageUrl());
                intent.putExtra("categoria", servicio.getCategoria());
                intent.putExtra("profesional", servicio.getProfesional());
                intent.putExtra("ubicacion", servicio.getUbicacion());
                // Agregar la lista de días disponibles al intent
                intent.putStringArrayListExtra("dias", (ArrayList<String>) servicio.getDias());
                mContext.startActivity(intent);
            }
        });

        container.addView(view);
        return view;
    }

    @Override
    public int getCount() {
        return mServicios.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    // Método para añadir nuevos ítems al ViewPager
    public void addItems(List<Servicio> newItems) {
        mServicios.addAll(newItems);
        notifyDataSetChanged(); // Notificar al adaptador del ViewPager que el conjunto de datos ha cambiado
    }
}
