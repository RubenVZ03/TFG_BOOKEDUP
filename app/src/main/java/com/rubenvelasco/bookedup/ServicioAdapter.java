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
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class ServicioAdapter extends RecyclerView.Adapter<ServicioAdapter.ServicioViewHolder> {

    private Context mContext;
    private List<Servicio> mServicios;

    public ServicioAdapter(Context context) {
        mContext = context;
        mServicios = new ArrayList<>();
    }

    public void setServicios(List<Servicio> servicios) {
        mServicios = servicios;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ServicioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_servicio_categoria, parent, false);
        return new ServicioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServicioViewHolder holder, int position) {
        Servicio servicio = mServicios.get(position);
        holder.bind(servicio);
    }

    @Override
    public int getItemCount() {
        return mServicios.size();
    }

    public class ServicioViewHolder extends RecyclerView.ViewHolder {

        private TextView nombreTextView;
        private TextView profesionalTextView;
        private Button btnMasInformacion;
        private ImageView imagenView;
        private TextView valoracionTextView;

        public ServicioViewHolder(@NonNull View itemView) {
            super(itemView);
            nombreTextView = itemView.findViewById(R.id.nombre);
            profesionalTextView = itemView.findViewById(R.id.profesional);
            btnMasInformacion = itemView.findViewById(R.id.btnMasInformacion);
            imagenView = itemView.findViewById(R.id.imagen);
            valoracionTextView = itemView.findViewById(R.id.valoracion);
        }

        public void bind(Servicio servicio) {
            nombreTextView.setText(servicio.getNombre());
            profesionalTextView.setText("con " + servicio.getProfesional());
            valoracionTextView.setText(String.valueOf(servicio.getValoracion()));

            // Cargar la imagen utilizando Picasso y redimensionarla con esquinas redondeadas
            int radius = 110; // El radio de las esquinas en píxeles
            int margin = 0; // Sin margen
            Transformation transformation = new RoundedCornersTransformation(radius, margin);

            Picasso.get()
                    .load(servicio.getImageUrl())
                    .fit()  // Ajustar la imagen al tamaño del ImageView
                    .transform(transformation)
                    .into(imagenView);

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
                    intent.putStringArrayListExtra("dias", servicio.getDias());
                    mContext.startActivity(intent);
                }
            });
        }
    }
}