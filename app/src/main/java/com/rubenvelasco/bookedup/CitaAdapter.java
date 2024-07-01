package com.rubenvelasco.bookedup;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CitaAdapter extends RecyclerView.Adapter<CitaAdapter.CitaViewHolder> {
    private List<Cita> citasList;

    public static class CitaViewHolder extends RecyclerView.ViewHolder {
        public TextView dia, hora, mes, profesional, servicio, estado, nombreServicio;

        public CitaViewHolder(View itemView) {
            super(itemView);
            dia = itemView.findViewById(R.id.dia);
            hora = itemView.findViewById(R.id.hora);
            mes = itemView.findViewById(R.id.mes);
            profesional = itemView.findViewById(R.id.profesional);
            nombreServicio = itemView.findViewById(R.id.nombreServicio);
            servicio = itemView.findViewById(R.id.servicio);
            estado = itemView.findViewById(R.id.estado);
        }
    }

    public CitaAdapter(List<Cita> citasList) {
        this.citasList = citasList;
    }

    @NonNull
    @Override
    public CitaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_appointment, parent, false);
        return new CitaViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CitaViewHolder holder, int position) {
        Cita cita = citasList.get(position);
        holder.dia.setText(cita.getDia());
        holder.hora.setText(cita.getHora());
        holder.mes.setText(cita.getMes());
        holder.profesional.setText("con " + cita.getProfesional());
        SpannableString spannableString = new SpannableString(cita.getServicio());
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        holder.servicio.setText(spannableString);

        holder.servicio.setText(cita.getServicio());
        holder.estado.setText(cita.getEstado());
    }

    @Override
    public int getItemCount() {
        return citasList.size();
    }
}
