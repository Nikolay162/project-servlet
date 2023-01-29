package com.tictactoe;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "LogicServlet", value = "/logic")
public class LogicServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //Получаем текущую сессию
        HttpSession currentSession = request.getSession();

        //Получаем объект игрового поля из сессии
        Field field = extractField(currentSession);

        //получаем индекс ячейки, по которой произошел отклик
        int index = getSelectedIndex(request);
        Sign currentSign = field.getField().get(index);
        //проверяем, что ячейка, по которой был клик пустая.
        //иначе ничего не делаем и отправляем пользователя на ту же страницу без изменений
        //параметров в сессии
        if (Sign.EMPTY != currentSign) {
            RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/index.jsp");
            dispatcher.forward(request, response);
            return;
        }

        //ставим крестик в ячейке, по которой кликнул пользователь
        field.getField().put(index, Sign.CROSS);

        //проверяем, не победил ли крестик после добавления последнего клика пользователя
        if (checkWin(response, currentSession,field)) {
            return;
        }

        //получаем пустую ячейку поля
        int emptyFieldIndex = field.getEmptyFieldIndex();

        if (emptyFieldIndex >= 0) {
            field.getField().put(emptyFieldIndex, Sign.NOUGHT);
            if (checkWin(response, currentSession,field)) {
                return;
            }
        }
        //если пустой ячейки нет и никто не победил - это значит ничья
        else {
            //добавляем в сессию флаг, который сигнализирует что произошла ничья
            currentSession.setAttribute("draw", true);

        }



        //считаем список значков
        List<Sign> data = field.getFieldData();

        //Обновляем объект поля и список значков в сессии
        currentSession.setAttribute("data", data);
        currentSession.setAttribute("field", field);

        response.sendRedirect("/index.jsp");
    }

    private int getSelectedIndex(HttpServletRequest request) {
        String click = request.getParameter("click");
        boolean isNumeric = click.chars().allMatch(Character::isDigit);
        return isNumeric ? Integer.parseInt(click) : 0;
    }

    private Field extractField(HttpSession currentSession) {
        Object fieldAttribute = currentSession.getAttribute("field");
        if (Field.class != fieldAttribute.getClass()) {
            currentSession.invalidate();
            throw new RuntimeException("Session is broken, try one more time");
        }
        return (Field) fieldAttribute;
    }
    //метод проверяет нет ли крестиков или ноликов в ряд
    //возвращает true или false
    private boolean checkWin (HttpServletResponse response, HttpSession currentSession, Field field) throws IOException {
        Sign winner = field.checkWin();
        if (Sign.CROSS==winner||Sign.NOUGHT==winner) {
            //добавляем флаг, который показывает кто победил
            currentSession.setAttribute("winner",winner);

            //считаем список значков
            List<Sign> data = field.getFieldData();

            //обновляем этот список в сессии
            currentSession.setAttribute("data", data);

            //шлем редирект
            response.sendRedirect("/index.jsp");
            return true;
            }
        return false;

    }
}
